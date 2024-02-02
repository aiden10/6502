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
        return "REGISTERS: {A: " + A + ", X: " + X + ", Y: " + Y + "}\n" +
                "FLAGS: " + Arrays.toString(flags) + "\n" +
                "POINTERS: {SP: " + SP + ", " + "PC: " + PC + "}";
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
        byte address = (byte) (zp_address + X);
        A = memory[address & 0xFF];
        LDA_FLAGS();
    }

    private byte fetch(){
        byte data = memory[PC];
        PC++;
        return data;
    }
    public void execute(){
        byte instruction = fetch();
        switch(instruction){
            case (byte) 0xA9:
                LDA_IMMEDIATE();
                break;

            case (byte) 0xA5:
                LDA_ZP();
                break;
            case (byte) 0xB5:
                LDA_ZP_X();
                break;
        }
    }
}
