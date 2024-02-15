An emulator for the 6502 processor. 

## Sample execution
![fill6502-ezgif com-video-to-gif-converter](https://github.com/aiden10/6502/assets/51337166/89c98bea-bcda-4777-810d-38ce27ff7fc9)

Here the emulator is running the assembled bytes of the program below which have been loaded into memory starting at address 0.  

```
        LDX #$FF      ; Load X register with the value 0xFF
        LDY #$00      ; Load Y register with the value 0x00 (initialize low byte)
FILL    STX $00FF,Y  ; Store the value in the X register at the memory address ($00FF + Y)
        INY           ; Increment Y register (move to the next memory address)
        BNE FILL      ; Branch back to FILL if Y is not zero
        BRK           ; Break (halt execution)
```
This program will fill the first 255 bytes in memory with the value 255.
