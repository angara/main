#!/usr/bin/env python3
# -*- coding: utf-8 -*-
# ;;
# ;; mongo "user" -> staging table m2p_user (stage 3 pre-check + merge source)
# ;;
# ;; input : EJSON lines from db.user (sorted by _id)
# ;; output: CSV for \copy into m2p_user(...), stats on stderr
# ;;
# ;; vk/fb top-level fields are NOT transferred (decision 2026-10-26);
# ;; they stay in the ejson/mongodump backups.
# ;;

import sys, csv, datetime

FIELDS = ["uid", "login", "email", "name", "surname", "town", "note",
          "passhash", "passcode", "atime", "ct", "ts", "upic", "admin", "auth_keys"]

LIMITS = {"login": 40, "email": 120, "name": 80, "surname": 40,
          "town": 80, "note": 4000, "upic": 200, "passhash": 80, "passcode": 20}


def conv_date(v):
    if isinstance(v, (int, float)):
        d = datetime.datetime.fromtimestamp(v / 1000.0, tz=datetime.timezone.utc)
    else:
        d = datetime.datetime.fromisoformat(v.replace("Z", "+00:00"))
    return d.strftime("%Y-%m-%d %H:%M:%S.%f") + "+00"


def arr_lit(items):
    return "{" + ",".join('"%s"' % i for i in items) + "}"


def main():
    w = csv.DictWriter(sys.stdout, fieldnames=FIELDS, lineterminator="\n")
    n = 0
    seen_uid = set()
    key_owner = {}
    auth_fmt_bad = []
    no_auth = 0
    no_passhash = 0
    social = 0
    for line in sys.stdin:
        line = line.strip()
        if not line:
            continue
        import json
        d = json.loads(line)
        uid = int(d["_id"])
        if uid in seen_uid:
            raise SystemExit("duplicate uid: %d" % uid)
        seen_uid.add(uid)

        auth = d.get("auth") or []
        if not isinstance(auth, list):
            raise SystemExit("auth is not an array, uid %d: %r" % (uid, auth))
        keys = []
        for a in auth:
            a = str(a)
            if ":" not in a:
                auth_fmt_bad.append((uid, a))
                continue
            t, _, v = a.partition(":")
            if t not in ("un", "em", "vk", "fb"):
                auth_fmt_bad.append((uid, a))
                continue
            if t == "vk" or t == "fb":
                continue                      # social logins not transferred
            if a in key_owner:
                raise SystemExit("auth key dup: %s uid %d and %d" % (a, key_owner[a], uid))
            key_owner[a] = uid
            keys.append(a)
        if not keys:
            no_auth += 1

        row = {
            "uid": uid,
            "login": d.get("username"),
            "email": d.get("email"),
            "name": d.get("first_name"),
            "surname": d.get("last_name"),
            "town": d.get("town"),
            "note": d.get("about"),
            "passhash": d.get("passhash"),
            "passcode": d.get("passcode"),
            "atime": conv_date(d["atime"]["$date"]) if isinstance(d.get("atime"), dict) else None,
            "ct": conv_date(d["ct"]["$date"]) if isinstance(d.get("ct"), dict) else None,
            "ts": conv_date(d["ts"]["$date"]) if isinstance(d.get("ts"), dict) else None,
            "upic": d.get("upic"),
            "admin": arr_lit(d["admin"]) if isinstance(d.get("admin"), list) else None,
            "auth_keys": arr_lit(keys) if keys else None,
        }
        if not row["passhash"]:
            no_passhash += 1
        if "vk" in d or "fb" in d:
            social += 1
        for f, lim in LIMITS.items():
            v = row[f]
            if v is not None and len(v) > lim:
                raise SystemExit("field too long (%s > %d): %r uid=%d"
                                 % (f, lim, v[:60], uid))
        w.writerow(row)
        n += 1
    print("docs: %d, no auth keys: %d, no passhash: %d, social(vk/fb) docs: %d, bad auth fmt: %d"
          % (n, no_auth, no_passhash, social, len(auth_fmt_bad)), file=sys.stderr)
    for x in auth_fmt_bad[:10]:
        print("  bad:", x, file=sys.stderr)


if __name__ == "__main__":
    main()
