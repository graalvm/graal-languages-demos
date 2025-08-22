global.crypto = {
    getRandomValues(buffer) {
        if (
            !buffer ||
            typeof buffer !== 'object' ||
            typeof buffer.length !== 'number' ||
            typeof buffer.BYTES_PER_ELEMENT !== 'number'
        ) {
            throw new TypeError(
                'Expected input to be an integer-indexed typed array'
            );
        }
        if (buffer.length > 65536) {
            throw new Error(
                'QuotaExceededError: The ArrayBufferView’s byte length exceeds the number of bytes of entropy available via this API'
            );
        }
        for (let i = 0; i < buffer.length; i++) {
            if (buffer.BYTES_PER_ELEMENT === 1) {
                buffer[i] = Math.floor(Math.random() * 256);
            } else if (buffer.BYTES_PER_ELEMENT === 2) {
                buffer[i] = Math.floor(Math.random() * 65536);
            } else if (buffer.BYTES_PER_ELEMENT === 4) {
                buffer[i] = Math.floor(Math.random() * Math.pow(2, 32));
            } else {
                throw new TypeError('Unsupported typed array type');
            }
        }
        return buffer;
    }
};