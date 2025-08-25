/*
 * Copyright (c) 2025, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */
use wasm_bindgen::prelude::*;

#[wasm_bindgen]
pub fn add(left: i32, right: i32) -> i32 {
    left + right
}

#[wasm_bindgen]
pub struct Person {
    name: String,
}

#[wasm_bindgen]
impl Person {
    pub fn say_hello(&self) -> String {
        format!("Hello, {}!", self.name)
    }
}

#[wasm_bindgen]
pub fn new_person(name: String) -> Person {
    Person { name }
}

#[wasm_bindgen]
pub fn reverse_string(input: String) -> String {
    input.chars().rev().collect()
}
