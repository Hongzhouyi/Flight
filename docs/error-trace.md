# Migration & Error Trace (Python -> Java)

This file keeps visible process traces, including errors, as requested.

## 1) Old Python entry no longer exists after migration

Command:

```bash
python3 app.py
```

Output:

```text
/root/.pyenv/versions/3.10.19/bin/python3: can't open file '/workspace/Flight/app.py': [Errno 2] No such file or directory
```

## 2) Wrong Java main class name (expected failure)

Command:

```bash
java -cp src Main
```

Output:

```text
Error: Could not find or load main class Main
Caused by: java.lang.ClassNotFoundException: Main
```

## 3) Correct Java build + test flow

Command:

```bash
javac src/FlightServer.java && bash tests/test_search.sh
```

Output:

```text
search test passed
```

## 4) Visual verification

Used Playwright to open the Java app and capture the results page screenshot artifact.
