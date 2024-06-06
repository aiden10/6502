Emulator for the 6502 processor. 

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
This program will fill the first 255 bytes in memory with the value 255 (Although it's not very clear).

## 6502 Info
3 8-bit general purpose registers: A, X, and Y.
Uses an 8-bit stack pointer and a 16-bit program counter, which stores the address of the next instruction.
Every cell/unit in memory is 8 bits.
The address width is 16 bits, meaning that 2^16 bytes of memory are addressable.
148 different instructions (not counting the including the different addressing modes for each one).

### Flags
The 6502 stores its flags in a single byte, although there are only 7 flags so one bit is wasted.
- Carry Flag (overflow or underflow from operation)
- Zero Flag (last operation resulted in a zero)
- Interrupt Disable Flag (set by the SEI instruction and cleared with the CLI instruction)
- Decimal Mode (set and cleared with instructions. When set, addition and subtraction follow BCD rules)
- Break Command (causes an interrupt)
- Overflow flag (set if last operation resulted in an invalid two's complement result)
- Negative flag (set if last operation had last bit set to a 1)

### Addressing Modes
- Accumulator: Targets the A register.
- Immediate: The data that the instruction requires is located immediately after the opcode of the instruction.
- Implied: The instruction needs no specified data or address. Such as CLC or any instruction which clears or sets flags.
- Indexed non-memory: Instructions that use the data in the X or Y registers and not memory.
- Non-indexed memory: Instructions that don't use data in the X or Y registers.
The indexed non-memory addressing modes consist of: Relative, Absolute, Zero-Page, and Indirect.
The Non-indexed memory addressing modes consist of: Absolute Indexed, Zero-Page Indexed, and Indirect Indexed.
Zero-Page refers to addresses 0-255.
