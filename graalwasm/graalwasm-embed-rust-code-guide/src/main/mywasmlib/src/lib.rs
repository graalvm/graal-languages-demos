use wasm_bindgen::prelude::*;

#[wasm_bindgen]
pub fn add(left: i32, right: i32) -> i32 {
    left + right
}