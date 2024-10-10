 #include <stdio.h>

 void floyd() {
     int number = 1;
     int rows = 10;
     for (int i = 1; i <= rows; i++) {
         for (int j = 1; j <= i; j++) {
             printf("%d ", number);
             ++number;
         }
         printf(".\n");
     }
 }

 int main() {
     floyd();
     return 0;
 }
