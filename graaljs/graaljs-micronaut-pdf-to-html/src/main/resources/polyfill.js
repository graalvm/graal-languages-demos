globalThis.self = globalThis;
globalThis.DOMMatrix = function () {};
globalThis.URL = function () {};
globalThis.navigator = { userAgent: '', platform: '' };
globalThis.AbortController = function () {};
globalThis.AbortSignal = function () {};

globalThis.structuredClone = (obj) => {
  if (obj instanceof Uint8Array) {
    return new Uint8Array(obj);
  } else if (obj instanceof ArrayBuffer) {
    return obj.slice(0);
  } else if (Array.isArray(obj)) {
    return obj.map(item => globalThis.structuredClone(item));
  } else if (obj && typeof obj === 'object') {
    const copy = {};
    for (const key in obj) {
      copy[key] = globalThis.structuredClone(obj[key]);
    }
    return copy;
  }
  return obj;
};
globalThis.ReadableStream = class {
  constructor(underlyingSource) {
    this._queue = [];
    this._closed = false;
    this._pendingReadRequests = [];

    const controller = {
      enqueue: (chunk) => {
        if (this._closed) return;
        if (this._pendingReadRequests.length > 0) {
          const resolve = this._pendingReadRequests.shift();
          resolve({ done: false, value: chunk });
        } else {
          this._queue.push(chunk);
        }
      },
      close: () => {
        this._closed = true;
        while (this._pendingReadRequests.length > 0) {
          const resolve = this._pendingReadRequests.shift();
          resolve({ done: true });
        }
      },
      error: (e) => {
        this._closed = true;
        while (this._pendingReadRequests.length > 0) {
          const reject = this._pendingReadRequests.shift();
          reject(e);
        }
      }
    };

    if (underlyingSource?.start) {
      try {
        underlyingSource.start(controller);
      } catch (e) {
        controller.error(e);
      }
    }

    this._controller = controller;
  }

  getReader() {
    return {
      read: () => {
        if (this._queue.length > 0) {
          const chunk = this._queue.shift();
          return Promise.resolve({ done: false, value: chunk });
        }
        if (this._closed) {
          return Promise.resolve({ done: true });
        }
        return new Promise((resolve, reject) => {
          this._pendingReadRequests.push(resolve);
        });
      },
      releaseLock: () => {}
    };
  }
};
globalThis.javaBytesToArrayBuffer = function (javaBytes) {
  const len = javaBytes.length;
  const array = new Uint8Array(len);
  for (let i = 0; i < len; i++) {
    array[i] = javaBytes[i];
  }
  return array.buffer;
};
