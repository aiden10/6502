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
    private void LDA_FLAGS(){
        ZF = (A == 0);
        NF = (A & 0b10000000) > 0;
    }
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
        byte address = (byte) ((zp_address & 0xFF) + (X & 0xFF));
        A = memory[address & 0xFF];
        LDA_FLAGS();
    }
    private void LDA_ABS(){
        byte low_bytes = fetch();
        byte high_bytes = fetch();
        short address = (short) ((low_bytes & 0xFF) + (high_bytes & 0xFF)); // short here because the address is 16 bits
        A = memory[address & 0xFFFF ]; // but Java doesn't support unsigned so a bitmask is once again necessary.
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
        byte zp = fetch();
        short intermediate = (short) ((zp & 0xFF) + (X & 0xFF));
        byte low = memory[intermediate & 0xFFFF];
        byte high = memory[(intermediate & 0xFFFF) + 1];
        short address = (short) ((low & 0xFF) + (high & 0xFF));
        A = memory[address & 0xFFFF];
        LDA_FLAGS();
    }
    private void LDA_INDIRECT_Y(){
        byte zp = fetch();
        short intermediate = (short) ((zp & 0xFF) + (Y & 0xFF));
        byte low = memory[intermediate & 0xFFFF];
        byte high = memory[(intermediate & 0xFFFF) + 1];
        short address = (short) ((low & 0xFF) + (high & 0xFF));
        A = memory[address & 0xFFFF];
        LDA_FLAGS();
    }

    private byte fetch(){
        byte data = memory[PC];
        PC++;
        return data;
    }
    public void execute(){
        byte instruction = fetch();
        switch (instruction) {
            case (byte) 0xA9 -> LDA_IMMEDIATE();
            case (byte) 0xA5 -> LDA_ZP();
            case (byte) 0xB5 -> LDA_ZP_X();
            case (byte) 0xAD -> LDA_ABS();
            case (byte) 0xBD -> LDA_ABS_X();
            case (byte) 0xB9 -> LDA_ABS_Y();
            case (byte) 0xA1 -> LDA_INDIRECT_X();
            case (byte) 0xB1 -> LDA_INDIRECT_Y();
        }
    }
}
