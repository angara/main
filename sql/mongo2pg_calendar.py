#!/usr/bin/env python3
# -*- coding: utf-8 -*-
# ;;
# ;; mongo "calendar" -> postgres "calendar" (one-shot)
# ;;
# ;; input : EJSON lines, one document per line:
# ;;           mongosh --quiet "$URI" --eval \
# ;;             'db.calendar.find().sort({_id:1}).forEach(x=>print(EJSON.stringify(x)))'
# ;; output: CSV on stdout for:
# ;;           \copy calendar(id,uid,ct,ts,date,date_1,status,title,descr,thumb,link)
# ;;                 from 'x.csv' with (format csv)
# ;;
# ;; mongo status "" and null both -> NULL (code treats them identically). ;;

import sys, csv, datetime

LIMITS = {"status": 10, "title": 200, "thumb": 60, "link": 60}

FIELDS = ["id", "uid", "ct", "ts", "date", "date_1", "status",
          "title", "descr", "thumb", "link"]


def conv_date(v):
    if isinstance(v, (int, float)):          # ms epoch
        d = datetime.datetime.fromtimestamp(v / 1000.0, tz=datetime.timezone.utc)
    else:                                     # ISO string
        d = datetime.datetime.fromisoformat(v.replace("Z", "+00:00"))
    return d.strftime("%Y-%m-%d %H:%M:%S.%f") + "+00"


def conv(doc):
    out = {}
    for k, v in doc.items():
        if k == "_id":
            out["id"] = v["$oid"]
        elif k in ("ct", "ts", "date", "date_1"):
            out[k] = conv_date(v["$date"]) if isinstance(v, dict) else None
        elif k == "status":
            out[k] = v if v else None         # "" and null -> NULL
        elif isinstance(v, (int, float)):
            out[k] = str(v)
        else:
            out[k] = v
    row = {f: out.get(f) for f in FIELDS}
    row["uid"] = int(row["uid"])              # uid always a numeric string
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
        import json
        row = conv(json.loads(line))
        if row["id"] in seen:
            raise SystemExit("duplicate id: " + row["id"])
        seen.add(row["id"])
        w.writerow(row)
        n += 1
    print(str(n) + " rows", file=sys.stderr)


if __name__ == "__main__":
    main()
