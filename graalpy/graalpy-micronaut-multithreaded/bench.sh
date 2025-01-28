#!/bin/bash

set -ex

CONCURRENCY=5
WARMUP_ITERATIONS=10
ITERATIONS=150
MAX_POOL=5

handle_interrupt() {
    printf "\rSIGINT caught      "
    shutdown_mn
    sleep 1
    exit 1
}

shutdown_mn() {
    if [ -n "$HEY_PID" ]; then
        kill $HEY_PID || true
        sleep 2
        kill -9 $HEY_PID || true
        unset HEY_PID
    fi
    if [ -n "$MN_PID" ]; then
        kill $MN_PID
        sleep 4
        kill -9 $MN_PID || true
        unset MN_PID
    fi
}

trap 'handle_interrupt' SIGINT

for i in `seq 1 ${MAX_POOL}`; do
    # prepare
    sed -i "s/this([0-9])/this($i)/" src/main/java/graalpy/micronaut/multithreaded/PythonPool.java
    ./mvnw compile
    ./mvnw mn:run &
    MN_PID=$!

    # give some time for startup compiler threads to calm down
    sleep 20

    # warmup
    wi=$[ i * WARMUP_ITERATIONS ]
    hey -t 0 -n $wi -c $CONCURRENCY -m POST -H 'Content-Type: multipart/form-data; boundary=----WebKitFormBoundary0utPAr8hN1Atdedb' -T "multipart/form-data" -D post.txt http://localhost:8080/data_analysis_multi &
    HEY_PID=$!
    wait $HEY_PID
    unset HEY_PID

    # benchmark
    hey -t 0 -o csv -n $ITERATIONS -c $CONCURRENCY -m POST -H 'Content-Type: multipart/form-data; boundary=----WebKitFormBoundary0utPAr8hN1Atdedb' -T "multipart/form-data" -D post.txt http://localhost:8080/data_analysis_multi | tee "benchmark_pool_${i}_concurrency_${CONCURRENCY}_iterations_${ITERATIONS}.csv" &
    HEY_PID=$!
    wait $HEY_PID
    unset HEY_PID

    shutdown_mn
done
