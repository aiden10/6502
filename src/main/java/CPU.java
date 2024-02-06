import java.util.Arrays;

public class CPU {
/*
A, X, and Y registers (8 bits each)
7 flags (1 bit each)
Program counter (16 bits)
Stack pointer (8 bits)
*/
    byte A, X, Y, SP;
    short PC;
    boolean CF, ZF, IF, DMF, BF, OF, NF;
    byte[] memory;
    private static final int RESET_VECTOR = 0xFCFF; // maybe should keep as the separate bytes
    private static final int MAX_SIZE = 0xFF38;
    public CPU(byte[] memory){
        CF = ZF = IF = DMF = BF = OF = NF = false;
        PC = 0;
        this.memory = memory;
    }
    public String toString(){
        boolean[] flags = {CF, ZF, IF, DMF, BF, OF, NF };
        return "REGISTERS: {A: " + (A & 0xFF) + ", X: " + (X & 0xFF) + ", Y: " + (Y & 0xFF) + "}\n" +
                "FLAGS: " + Arrays.toString(flags) + "\n" +
                "POINTERS: {SP: " + (SP & 0xFF) + ", " + "PC: " + (PC & 0xFFFF) + "}";
    }

    // FLAGS
    private void LDA_FLAGS(){
        ZF = (A == 0);
        NF = (A & 0b10000000) > 0;
    }
    private void LDX_FLAGS(){
        ZF = (X == 0);
        NF = (X & 0b10000000) > 0;
    }
    private void LDY_FLAGS(){
        ZF = (Y == 0);
        NF = (Y & 0b10000000) > 0;
    }

    // ADDRESSING MODES
    /*
        Zero Page: fetch the next byte and use it as address. The byte will have a value from 0-255.
        Zero Page X/Y; fetch the next byte and add X/Y to it. I think the sum will still be 0-255 which is why it's zero page.
        Absolute: add up the next two bytes in memory to get a new 16-bit address.
        Absolute X/Y: fetch next byte and add it to value in the X/Y register to get a new 16-bit address.
        Immediate: fetch the next byte and use it; not as an address but as the actual value.
        Indirect X/Y: fetch the next byte (zero page) and add it to the X/Y register to get a new intermediate 16-bit address.
                      fetch the byte at that intermediate address and the byte at the intermediate address + 1. Sum the low and
                      high bytes to get the final address.
        Implied: user specifies nothing other than the instruction. The instruction always does the same thing (clear flag, transfer from register to register, etc.).

    */
    private short ABS_ADDRESS(){
        byte low_bytes = fetch();
        byte high_bytes = fetch();
        return (short) ((low_bytes & 0xFF) + (high_bytes & 0xFF));
    }
    private short INDIRECT_X_ADDRESS(){
        byte zp = fetch();
        short intermediate = (short) ((zp & 0xFF) + (X & 0xFF));
        byte low = memory[intermediate & 0xFFFF];
        byte high = memory[(intermediate & 0xFFFF) + 1];
        return (short) ((low & 0xFF) + (high & 0xFF));
    }
    private short INDIRECT_Y_ADDRESS(){
        byte zp = fetch();
        short intermediate = (short) ((zp & 0xFF) + (Y & 0xFF));
        byte low = memory[intermediate & 0xFFFF];
        byte high = memory[(intermediate & 0xFFFF) + 1];
        return (short) ((low & 0xFF) + (high & 0xFF));
    }

    // LDA
    private void LDA_IMMEDIATE(){
        A = fetch(); // load with byte after instruction
        LDA_FLAGS();
    }
    private void LDA_ZP(){
        byte address = fetch();
        A = memory[address & 0xFF];
        LDA_FLAGS();
    }
    private void LDA_ZP_X(){
        byte zp_address = fetch();
        short address = (short) ((zp_address & 0xFF) + (X & 0xFF));
        A = memory[address & 0xFFFF];
        LDA_FLAGS();
    }
    private void LDA_ABS(){
        A = memory[ABS_ADDRESS() & 0xFFFF ]; // but Java doesn't support unsigned so a bitmask is once again necessary.
        LDA_FLAGS();
    }
    private void LDA_ABS_X(){
        byte low_bytes = fetch();
        short address = (short) ((low_bytes & 0xFF) + (X & 0xFF));
        A = memory[address & 0xFFFF ];
        LDA_FLAGS();
    }
    private void LDA_ABS_Y(){
        byte low_bytes = fetch();
        short address = (short) ((low_bytes & 0xFF) + (Y & 0xFF));
        A = memory[address & 0xFFFF ];
        LDA_FLAGS();
    }
    private void LDA_INDIRECT_X(){
        A = memory[INDIRECT_X_ADDRESS() & 0xFFFF];
        LDA_FLAGS();
    }
    private void LDA_INDIRECT_Y(){
        A = memory[INDIRECT_Y_ADDRESS() & 0xFFFF];
        LDA_FLAGS();
    }

    // LDX
    private void LDX_IMMEDIATE(){
        X = fetch();
        LDX_FLAGS();
    }
    private void LDX_ZP(){
        byte zp = fetch();
        X = memory[zp & 0xFF];
        LDX_FLAGS();
    }
    private void LDX_ZP_Y(){
        byte zp = fetch();
        short address = (short) ((zp & 0xFF) + (Y & 0xFF));
        X = memory[address & 0xFFFF];
        LDX_FLAGS();
    }
    private void LDX_ABS(){
        X = memory[ABS_ADDRESS() & 0xFFFF];
        LDX_FLAGS();
    }
    private void LDX_ABS_Y(){
        byte low_bytes = fetch();
        short address = (short) ((low_bytes & 0xFF) + (Y & 0xFF));
        X = memory[address & 0xFFFF ];
        LDX_FLAGS();
    }

    // LDY
    private void LDY_IMMEDIATE(){
        Y = fetch();
        LDY_FLAGS();
    }
    private void LDY_ZP(){
        byte zp = fetch();
        Y = memory[zp & 0xFF];
        LDY_FLAGS();
    }
    private void LDY_ZP_X(){
        byte zp = fetch();
        short address = (short) ((zp & 0xFF) + (X & 0xFF));
        Y = memory[address & 0xFFFF];
        LDY_FLAGS();
    }
    private void LDY_ABS(){
        Y = memory[ABS_ADDRESS() & 0xFFFF];
        LDY_FLAGS();
    }
    private void LDY_ABS_X(){
        byte low_bytes = fetch();
        short address = (short) ((low_bytes & 0xFF) + (X & 0xFF));
        Y = memory[address & 0xFFFF ];
        LDX_FLAGS();
    }

    // STA
    private void STA_ZP(){
        byte address = fetch();
        memory[address & 0xFF] = A;
    }
    private void STA_ZP_X(){
        byte zp = fetch();
        short address = (short) ((zp & 0xFF) + (X & 0xFF));
        memory[address & 0xFFFF] = A;
    }
    private void STA_ABS(){
        byte low_bytes = fetch();
        byte high_bytes = fetch();
        short address = (short) ((low_bytes & 0xFF) + (high_bytes & 0xFF));
        memory[address & 0xFFFF] = A;
    }
    private void STA_ABS_X(){
        byte low_bytes = fetch();
        short address = (short) ((low_bytes & 0xFF) + (X & 0xFF));
        memory[address & 0xFFFF] = A;
    }
    private void STA_ABS_Y(){
        byte low_bytes = fetch();
        short address = (short) ((low_bytes & 0xFF) + (Y & 0xFF));
        memory[address & 0xFFFF] = A;
    }
    private void STA_INDIRECT_X(){
        memory[INDIRECT_X_ADDRESS() & 0xFFFF] = A;
    }
    private void STA_INDIRECT_Y(){
        memory[INDIRECT_Y_ADDRESS() & 0xFFFF] = A;
    }

    // STX
    private void STX_ZP(){
        byte address = fetch();
        memory[address & 0xFF] = X;
    }
    private void STX_ZP_Y(){
        byte zp = fetch();
        short address = (short) ((zp & 0xFF) + (Y & 0xFF));
        memory[address & 0xFFFF] = X;
    }
    private void STX_ABS(){
        memory[ABS_ADDRESS() & 0xFFFF] = X;
    }

    // STY
    private void STY_ZP(){
        byte address = fetch();
        memory[address & 0xFF] = Y;
    }
    private void STY_ZP_X(){
        byte zp = fetch();
        short address = (short) ((zp & 0xFF) + (X & 0xFF));
        memory[address & 0xFFFF] = Y;
    }
    private void STY_ABS(){
        memory[ABS_ADDRESS() & 0xFFFF] = Y;
    }

    // TAX
    private void TAX(){
        X = A;
        LDA_FLAGS();
    }
    // TAY
    private void TAY(){
        Y = A;
        LDA_FLAGS();
    }
    // TSX
    private void TSX(){
        X = SP;
        LDX_FLAGS();
    }
    // TXA
    private void TXA(){
        A = X;
        LDX_FLAGS();
    }
    // TXS
    private void TXS(){
        SP = X;
    }
    // TYA
    private void TYA(){
        A = Y;
        LDY_FLAGS();
    }

    // SEC
    private void SEC(){
        CF = true;
    }
    // SED
    private void SED(){
        DMF = true;
    }
    private void SEI(){
        IF = true;
    }

    private byte fetch(){
        byte data = memory[PC];
        PC++;
        return data;
    }
    public void execute(){
        byte instruction = fetch();
        switch (instruction) {

            // LDA
            case (byte) 0xA9 -> LDA_IMMEDIATE();
            case (byte) 0xA5 -> LDA_ZP();
            case (byte) 0xB5 -> LDA_ZP_X();
            case (byte) 0xAD -> LDA_ABS();
            case (byte) 0xBD -> LDA_ABS_X();
            case (byte) 0xB9 -> LDA_ABS_Y();
            case (byte) 0xA1 -> LDA_INDIRECT_X();
            case (byte) 0xB1 -> LDA_INDIRECT_Y();

            // LDX
            case (byte) 0xA2 -> LDX_IMMEDIATE();
            case (byte) 0xA6 -> LDX_ZP();
            case (byte) 0xB6 -> LDX_ZP_Y();
            case (byte) 0xAE -> LDX_ABS();
            case (byte) 0xBE -> LDX_ABS_Y();

            // LDY
            case (byte) 0xA0 -> LDY_IMMEDIATE();
            case (byte) 0xA4 -> LDY_ZP();
            case (byte) 0xB4 -> LDY_ZP_X();
            case (byte) 0xAC -> LDY_ABS();
            case (byte) 0xBC -> LDY_ABS_X();

            // STA
            case (byte) 0x85 -> STA_ZP();
            case (byte) 0x95 -> STA_ZP_X();
            case (byte) 0x8D -> STA_ABS();
            case (byte) 0x9D -> STA_ABS_X();
            case (byte) 0x99 -> STA_ABS_Y();
            case (byte) 0x81 -> STA_INDIRECT_X();
            case (byte) 0x91 -> STA_INDIRECT_Y();

            // STX
            case (byte) 0x86 -> STX_ZP();
            case (byte) 0x96 -> STX_ZP_Y();
            case (byte) 0x8E -> STX_ABS();

            // STY
            case (byte) 0x84 -> STY_ZP();
            case (byte) 0x94 -> STY_ZP_X();
            case (byte) 0x8C -> STY_ABS();

            // TAX
            case (byte) 0xAA -> TAX();

            // TAY
            case (byte) 0xA8 -> TAY();

            // TSX
            case (byte) 0xBA -> TSX();

            // TXA
            case (byte) 0x8A -> TXA();

            // TXS
            case (byte) 0x9A -> TXS();

            // TYA
            case (byte) 0x98 -> TYA();

            // SEC
            case (byte) 0x38 -> SEC();

            // SED
            case (byte) 0xF8 -> SED();

            // SEI
            case (byte) 0x78 -> SEI();
        }
    }
}
