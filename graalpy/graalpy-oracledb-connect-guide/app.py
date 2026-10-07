# Copyright (c) 2025-2026, Oracle and/or its affiliates. All rights reserved.
import os

from flask import Flask, jsonify
import oracledb

app = Flask(__name__)

ORACLE_USER = os.getenv("ORACLE_USER", "appuser")
ORACLE_PASSWORD = os.getenv("ORACLE_PASSWORD", "apppassword")
ORACLE_DSN = os.getenv("ORACLE_DSN", "localhost:1521/freepdb1")
PORT = int(os.getenv("PORT", "8080"))


@app.route("/")
def db_example():
    try:
        with oracledb.connect(
            user=ORACLE_USER,
            password=ORACLE_PASSWORD,
            dsn=ORACLE_DSN,
        ) as conn:
            with conn.cursor() as cur:
                cur.execute("select 'Hello from Oracle' as msg from dual")
                row = cur.fetchone()
                return jsonify({"message": row[0] if row else None})
    except Exception as exc:
        return jsonify({"error": str(exc)}), 500


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=PORT, use_reloader=False)
