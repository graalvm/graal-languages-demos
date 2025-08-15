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
    #[wasm_bindgen]
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