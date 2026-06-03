(async () => {
  global = globalThis;
  const nowOffset = Date.now();
  const now = () => Date.now() - nowOffset;
  global.process = {};
  global.nodeCrypto = {};
  global.process.hrtime = global.process.hrtime || ((previousTimestamp) => {
    const baseNow = Math.floor((Date.now() - now()) * 1e-3)
    const clocktime = now() * 1e-3
    let seconds = Math.floor(clocktime) + baseNow
    let nanoseconds = Math.floor((clocktime % 1) * 1e9)

    if (previousTimestamp) {
      seconds = seconds - previousTimestamp[0]
      nanoseconds = nanoseconds - previousTimestamp[1]
      if (nanoseconds < 0) {
        seconds--
        nanoseconds += 1e9
      }
    }
    return [seconds, nanoseconds]
  });
  global.nodeCrypto.randomFillSync = function(number) {
    return 123;
  };
})();


