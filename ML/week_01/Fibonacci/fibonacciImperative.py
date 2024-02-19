def fib(n):
    sum = 0
    a = 0
    b = 1

    while(n > 1):
        sum = a + b
        a = b
        b = sum
        n = n-1
    
    return sum

print(fib(8))