# 20180307-AmitSilvadasan-ISSPass
The API returns a list of upcoming ISS passes for a particular location formatted as JSON.

As input it expects a latitude/longitude pair, altitude and how many results to return. All fields are required.
As output you get the same inputs back (for checking) and a time stamp when the API ran in addition to a
success or failure message and a list of passes. Each pass has a duration in seconds and a rise time as a unix
time stamp.

Key features:
1. No Sonarqube Blocker and Critical issues.
2. Cognitive complexity of method is less than 15.
3. API is build in MobileFirst Platform Server using a Native API adapter.
4. Logging and graceful handling of Exception.
