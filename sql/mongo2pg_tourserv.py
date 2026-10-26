#!/usr/bin/env python3
# -*- coding: utf-8 -*-
# ;;
# ;; mongo "tourserv" -> postgres "tourserv" (one-shot, idempotent-friendly)
# ;;
# ;; input : EJSON lines, one document per line:
# ;;           mongosh --quiet "$URI" --eval \
# ;;             'db.tourserv.find().sort({_id:1}).forEach(x=>print(EJSON.stringify(x)))'
# ;;         (relaxed EJSON: {"$oid": "..."}, {"$date": "ISO"})
# ;; output: CSV on stdout for:
# ;;           \copy tourserv(id,uid,ct,ts,type,town,addr,title,descr,link,
# ;;                          payload,price,email,phone,person,note,flags)
# ;;                          from 'x.csv' with (format csv)
# ;;
# ;; unquoted empty CSV field -> NULL, quoted "" -> empty string;
# ;; python csv (QUOTE_MINIMAL) writes None as unquoted empty. ;;

import sys, csv, datetime

# varchar limits from tourserv DDL
LIMITS = {"type": 10, "town": 60, "addr": 120, "title": 200,
          "link": 200, "email": 120, "phone": 200, "person": 120}

FIELDS = ["id", "uid", "ct", "ts", "type", "town", "addr", "title", "descr",
          "link", "payload", "price", "email", "phone", "person", "note", "flags"]


def conv_date(v):
    if isinstance(v, (int, float)):          # ms epoch
        d = datetime.datetime.fromtimestamp(v / 1000.0, tz=datetime.timezone.utc)
    else:                                     # ISO string, e.g. 2017-04-04T12:52:03.117Z
        d = datetime.datetime.fromisoformat(v.replace("Z", "+00:00"))
    return d.strftime("%Y-%m-%d %H:%M:%S.%f") + "+00"


def conv(doc):
    out = {}
    for k, v in doc.items():
        if k == "_id":
            out["id"] = v["$oid"]
        elif k in ("ct", "ts"):
            out[k] = conv_date(v["$date"]) if isinstance(v, dict) else None
        elif k == "flags":
            out[k] = ("{" + ",".join('"%s"' % f for f in v) + "}") if v else "{}"
        elif isinstance(v, (int, float)):
            out[k] = str(v)                    # e.g. phone stored as number in one doc
        else:
            out[k] = v
    row = {f: out.get(f) for f in FIELDS}
    if not row["flags"]:
        row["flags"] = "{}"
    for f, lim in LIMITS.items():
        v = row[f]
        if v is not None and len(v) > lim:
            raise SystemExit("field too long (%s > %d): %r id=%s"
                             % (f, lim, v[:80], row["id"]))
    return row


def main():
    w = csv.DictWriter(sys.stdout, fieldnames=FIELDS, lineterminator="\n")
    n = 0
    seen = set()
    for line in sys.stdin:
        line = line.strip()
        if not line:
            continue
        row = conv(json_loads(line))
        if row["id"] in seen:
            raise SystemExit("duplicate id: " + row["id"])
        seen.add(row["id"])
        w.writerow(row)
        n += 1
    print(str(n) + " rows", file=sys.stderr)


def json_loads(s):
    import json
    return json.loads(s)


if __name__ == "__main__":
    main()
