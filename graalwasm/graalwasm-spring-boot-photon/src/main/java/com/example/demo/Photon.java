/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example.demo;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.io.ByteSequence;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public record Photon(Value module, Uint8Array imageContent) {

    boolean implementsEffect(String effectName) {
        return module.hasMember(effectName);
    }

    void applyEffect(String effectName, PhotonImage image) {
        module.invokeMember(effectName, image);
    }

    PhotonImage createImage() {
        PhotonImage photonImage = module.getMember("PhotonImage").as(PhotonImage.class);
        return photonImage.new_from_byteslice(imageContent);
    }

    public interface PhotonImage {
        void free();

        Uint8Array get_bytes();

        PhotonImage new_from_byteslice(Uint8Array imageContent);
    }

    public interface Uint8Array {
        ByteSequence buffer();
    }

    public static byte[] toByteArray(PhotonImage photonImage) {
        return photonImage.get_bytes().buffer().toByteArray();
    }

    static class PhotonRuntimeHints implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.proxies().registerJdkProxy(PhotonImage.class);
        }
    }
}
