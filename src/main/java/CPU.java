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
    private static final int RESET_VECTOR_LOW = 0xFFFC;
    private static final int RESET_VECTOR_HIGH = 0xFFFD;

    private static final int NMI_LOW = 0xFFFA;
    private static final int NMI_HIGH = 0xFFFB;

    private static final int IRQ_LOW = 0xFFFE;
    private static final int IRQ_HIGH = 0xFFFF;


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
    private void CLEAR_FLAGS(){
        CF = false;
        ZF = false;
        IF = false;
        DMF = false;
        BF = false;
        OF = false;
        NF = false;
    }
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
    private void INC_FLAGS(short address){
        ZF = (memory[address & 0xFFFF] == 0);
        NF = (memory[address & 0xFFFF] & 0b10000000) > 0;
    }
    private void CMP_FLAGS(byte value){
        ZF = (A == (value & 0xFF));
        NF = (A & 0b10000000) > 0;
        CF = (A >= (value & 0xFF));
    }
    private void CPX_FLAGS(byte value){
        ZF = (X == (value & 0xFF));
        NF = (X & 0b10000000) > 0;
        CF = (X >= (value & 0xFF));
    }
    private void CPY_FLAGS(byte value){
        ZF = (Y == (value & 0xFF));
        NF = (Y & 0b10000000) > 0;
        CF = (Y >= (value & 0xFF));
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
        A = memory[ABS_ADDRESS() & 0xFFFF ];
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
    // SEI
    private void SEI(){
        IF = true;
    }
    // CLC
    private void CLC(){
        CF = false;
    }
    // CLD
    private void CLD(){
        DMF = false;
    }
    // CLI
    private void CLI(){
        IF = false;
    }

    // INX
    private void INX(){
        X++;
        LDX_FLAGS();
    }
    // INY
    private void INY(){
        Y++;
        LDY_FLAGS();
    }

    // INC
    private void INC_ZP(){
        byte zp = fetch();
        memory[zp & 0xFF]++;
        INC_FLAGS(zp);
    }
    private void INC_ZP_X(){
        byte zp_address = fetch();
        short address = (short) ((zp_address & 0xFF) + (X & 0xFF));
        memory[address & 0xFFFF]++;
        INC_FLAGS(zp_address);
    }
    private void INC_ABS(){
        short address = ABS_ADDRESS();
        memory[address & 0xFFFF]++;
        INC_FLAGS(address);
    }
    private void INC_ABS_X(){
        byte low_bytes = fetch();
        short address = (short) ((low_bytes & 0xFF) + (X & 0xFF));
        memory[address & 0xFFFF]++;
        INC_FLAGS(address);
    }

    // DEC
    private void DEC_ZP(){
        byte zp = fetch();
        memory[zp & 0xFF]--;
        INC_FLAGS(zp);
    }
    private void DEC_ZP_X(){
        byte zp_address = fetch();
        short address = (short) ((zp_address & 0xFF) + (X & 0xFF));
        memory[address & 0xFFFF]--;
        INC_FLAGS(zp_address);
    }
    private void DEC_ABS(){
        short address = ABS_ADDRESS();
        memory[address & 0xFFFF]--;
        INC_FLAGS(address);
    }
    private void DEC_ABS_X(){
        byte low_bytes = fetch();
        short address = (short) ((low_bytes & 0xFF) + (X & 0xFF));
        memory[address & 0xFFFF]--;
        INC_FLAGS(address);
    }
    // DEX
    private void DEX(){
        X--;
        LDX_FLAGS();
    }
    // DEY
    private void DEY(){
        Y--;
        LDY_FLAGS();
    }

    // CMP
    private void CMP_IMMEDIATE(){ // 2 cycles
        CMP_FLAGS(fetch());
    }
    private void CMP_ZP(){ // 3 cycles
        byte zp_address = fetch();
        CMP_FLAGS(memory[zp_address & 0xFF]);
    }
    private void CMP_ZP_X(){ // 4 cycles
        byte zp_address = fetch();
        short address = (short)((zp_address & 0xFF) + (X & 0xFF));
        CMP_FLAGS(memory[address & 0xFFFF]);
    }
    private void CMP_ABS(){ // 4 cycles
        CMP_FLAGS(memory[ABS_ADDRESS() & 0xFFFF]);
    }
    private void CMP_ABS_X(){ // 4* cycles
        byte low_bytes = fetch();
        short address = (short) ((low_bytes & 0xFF) + (X & 0xFF));
        CMP_FLAGS(memory[address & 0xFFFF]);
    }
    private void CMP_ABS_Y(){ // 4* cycles
        byte low_bytes = fetch();
        short address = (short) ((low_bytes & 0xFF) + (Y & 0xFF));
        CMP_FLAGS(memory[address & 0xFFFF]);
    }
    private void CMP_INDIRECT_X(){ // 6 cycles
        CMP_FLAGS(memory[INDIRECT_X_ADDRESS() & 0xFFFF]);
    }
    private void CMP_INDIRECT_Y(){ // 5* cycles
        CMP_FLAGS(memory[INDIRECT_Y_ADDRESS() & 0xFFFF]);
    }

    // CPX
    private void CPX_IMMEDIATE(){ // 2 cycles
        CPX_FLAGS(fetch());
    }
    private void CPX_ZP(){ // 3 cycles
        byte zp_address = fetch();
        CPX_FLAGS(memory[zp_address & 0xFF]);
    }
    private void CPX_ABS(){ // 4 cycles
        CPX_FLAGS(memory[ABS_ADDRESS() & 0xFFFF]);
    }

    // CPY
    private void CPY_IMMEDIATE(){ // 2 cycles
        CPY_FLAGS(fetch());
    }
    private void CPY_ZP(){ // 3 cycles
        byte zp_address = fetch();
        CPY_FLAGS(memory[zp_address & 0xFF]);
    }
    private void CPY_ABS(){ // 4 cycles
        CPY_FLAGS(memory[ABS_ADDRESS() & 0xFFFF]);
    }

    // Branches
    private void BCC(){ // 2** cycles
        if (!CF){
            PC += (int) fetch();
        }
    }
    private void BCS(){ // 2** cycles
        if (CF){
            PC += (int) fetch();
        }
    }
    private void BEQ(){ // 2** cycles
        if (ZF){
            PC += (int) fetch();
        }
    }
    private void BMI(){ // 2** cycles
        if (NF){
            PC += (int) fetch();
        }
    }
    private void BNE(){ // 2** cycles
        if (!ZF){
            PC += (int) fetch();
        }
    }
    private void BPL(){ // 2** cycles
        if (ZF){
            PC += (int) fetch();
        }
    }
    private void BVC(){ // 2** cycles
        if (!OF){
            PC += (int) fetch();
        }
    }
    private void BVS(){ // 2** cycles
        if (OF){
            PC += (int) fetch();
        }
    }

    // PHA
    private void PHA(){ // 3 cycles
        SP -= 1;
        memory[SP & 0xFF] = A;
    }
    // PLA
    private void PLA(){ // 4 cycles
        A = memory[SP & 0xFF];
        LDA_FLAGS();
    }

    // BIT
    private void BIT_ZP(){ // 3 cycles
        byte zp_address = fetch();
        byte value = memory[zp_address & 0xFF];
        NF = (value & 0b10000000) != 0;
        OF = (value & 0b01000000) != 0;
        ZF = (value & A) == 0;
    }
    private void BIT_ABS(){ // 4 cycles
        short address = ABS_ADDRESS();
        byte value = memory[address & 0xFFFF];
        NF = (value & 0b10000000) != 0;
        OF = (value & 0b01000000) != 0;
        ZF = (value & A) == 0;

    }

    // RTS
    private void RTS(){ // 6 cycles
        SP++;
        byte low = memory[SP & 0xFF];
        SP++;
        byte high = memory[SP & 0xFF];
        PC += ((low & 0xFF) + (high & 0xFF));
    }
    private byte fetch(){
        byte data = memory[PC];
        PC++;
        return data;
    }
    public void execute(){
        CLEAR_FLAGS();
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
            // CLC
            case (byte) 0x18 -> CLC();
            // CLD
            case (byte) 0xD8 -> CLD();
            // CLI
            case (byte) 0x58 -> CLI();

            // INC
            case (byte) 0xE6 -> INC_ZP();
            case (byte) 0xF6 -> INC_ZP_X();
            case (byte) 0xEE -> INC_ABS();
            case (byte) 0xFE -> INC_ABS_X();

            // INX
            case (byte) 0xE8 -> INX();

            // INY
            case (byte) 0xC8 -> INY();

            // DEC
            case (byte) 0xC6 -> DEC_ZP();
            case (byte) 0xD6 -> DEC_ZP_X();
            case (byte) 0xCE -> DEC_ABS();
            case (byte) 0xDE -> DEC_ABS_X();

            // DEX
            case (byte) 0xCA -> DEX();

            // DEY
            case (byte) 0x88 -> DEY();

            // CMP
            case (byte) 0xC9 -> CMP_IMMEDIATE();
            case (byte) 0xC5 -> CMP_ZP();
            case (byte) 0xD5 -> CMP_ZP_X();
            case (byte) 0xCD -> CMP_ABS();
            case (byte) 0xDD -> CMP_ABS_X();
            case (byte) 0xD9 -> CMP_ABS_Y();
            case (byte) 0xC1 -> CMP_INDIRECT_X();
            case (byte) 0xD1 -> CMP_INDIRECT_Y();

            // CPX
            case (byte) 0xE0 -> CPX_IMMEDIATE();
            case (byte) 0xE4 -> CPX_ZP();
            case (byte) 0xEC -> CPX_ABS();

            // CPY
            case (byte) 0xC0 -> CPY_IMMEDIATE();
            case (byte) 0xC4 -> CPY_ZP();
            case (byte) 0xCC -> CPY_ABS();

            // BCC
            case (byte) 0x90 -> BCC();

            // BCS
            case (byte) 0xB0 -> BCS();

            // BEQ
            case (byte) 0xF0 -> BEQ();

            // BMI
            case (byte) 0x30 -> BMI();

            // BNE
            case (byte) 0xD0 -> BNE();

            // BPL
            case (byte) 0x10 -> BPL();

            // BVC
            case (byte) 0x50 -> BVC();

            // BVS
            case (byte) 0x70 -> BVS();

            // PHA
            case (byte) 0x48 -> PHA();

            // PLA
            case (byte) 0x68 -> PLA();

            // BIT
            case (byte) 0x24 -> BIT_ZP();
            case(byte) 0x2C -> BIT_ABS();

            // RTS
            case (byte) 0x60 -> RTS();
        }
    }
}
