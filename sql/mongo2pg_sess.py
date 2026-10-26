#!/usr/bin/env python3
# -*- coding: utf-8 -*-
# ;;
# ;; mongo "sess" -> postgres "sess" (one-shot, run in the cutover window)
# ;;
# ;; input : EJSON lines from db.sess (sorted by _id)
# ;; output: CSV for \copy sess(sid,uid,ct,ts,ip,tmp,csrf)
# ;;

import sys, csv, datetime

FIELDS = ["sid", "uid", "ct", "ts", "ip", "tmp", "csrf"]


def conv_date(v):
    if isinstance(v, (int, float)):
        d = datetime.datetime.fromtimestamp(v / 1000.0, tz=datetime.timezone.utc)
    else:
        d = datetime.datetime.fromisoformat(v.replace("Z", "+00:00"))
    return d.strftime("%Y-%m-%d %H:%M:%S.%f") + "+00"


def main():
    w = csv.DictWriter(sys.stdout, fieldnames=FIELDS, lineterminator="\n")
    n = 0
    nonnum = 0
    for line in sys.stdin:
        line = line.strip()
        if not line:
            continue
        import json
        d = json.loads(line)
        uid = d.get("uid")
        try:
            uid = int(uid)
        except (TypeError, ValueError):
            nonnum += 1
            uid = None
        w.writerow({
            "sid": d["_id"]["$oid"] if isinstance(d.get("_id"), dict) else d["_id"],
            "uid": uid,
            "ct": conv_date(d["ct"]["$date"]),
            "ts": conv_date(d["ts"]["$date"]),
            "ip": d.get("ip"),
            "tmp": "t" if d.get("tmp") else "f",
            "csrf": d.get("_csrf"),
        })
        n += 1
    print("%d rows, %d non-numeric uid" % (n, nonnum), file=sys.stderr)


if __name__ == "__main__":
    main()
