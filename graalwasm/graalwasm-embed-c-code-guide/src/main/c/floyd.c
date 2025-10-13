#include <stdio.h>

extern int javaInc(int number)
__attribute__((
    __import_module__("env"),
    __import_name__("java-increment"),
));

void floyd(int rows) {
    int number = 1;
    for (int i = 1; i <= rows; i++) {
        for (int j = 1; j <= i; j++) {
            printf("%d ", number);
            number = javaInc(number);
        }
        printf(".\n");
    }
}

int main() {
    floyd(10);
    return 0;
}
