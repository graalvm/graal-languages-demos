#include <stdio.h>

void floyd(int rows) {
    int number = 1;
    for (int i = 1; i <= rows; i++) {
        for (int j = 1; j <= i; j++) {
            printf("%d ", number);
            ++number;
        }
        printf(".\n");
    }
}

int main() {
    floyd(10);
    return 0;
}
