library IEEE;
use IEEE.STD_LOGIC_1164.ALL;

entity test_env is
    Port ( clk : in STD_LOGIC;
           btn : in STD_LOGIC_VECTOR (4 downto 0);
           sw : in STD_LOGIC_VECTOR (15 downto 0);
           led : out STD_LOGIC_VECTOR (15 downto 0);
           an : out STD_LOGIC_VECTOR (3 downto 0);
           cat : out STD_LOGIC_VECTOR (6 downto 0));
end test_env;

architecture Behavioral of test_env is

component MPG is
    Port ( en : out STD_LOGIC;
           input : in STD_LOGIC;
           clock : in STD_LOGIC);
end component;

component SSD is
    Port ( clk: in STD_LOGIC;
           digits: in STD_LOGIC_VECTOR(15 downto 0);
           an: out STD_LOGIC_VECTOR(3 downto 0);
           cat: out STD_LOGIC_VECTOR(6 downto 0));
end component;

component IFetch
    Port ( clk: in STD_LOGIC;
           rst : in STD_LOGIC;
           en : in STD_LOGIC;
           BranchAddress : in STD_LOGIC_VECTOR(15 downto 0);
           JumpAddress : in STD_LOGIC_VECTOR(15 downto 0);
           Jump : in STD_LOGIC;
           PCSrc : in STD_LOGIC;
           Instruction : out STD_LOGIC_VECTOR(15 downto 0);
           PCinc : out STD_LOGIC_VECTOR(15 downto 0));
end component;

component IDecode
    Port ( clk: in STD_LOGIC;
           en : in STD_LOGIC;    
           Instr : in STD_LOGIC_VECTOR(12 downto 0);
           WD : in STD_LOGIC_VECTOR(15 downto 0);
           RegWrite : in STD_LOGIC;
           ExtOp : in STD_LOGIC;
           RD1 : out STD_LOGIC_VECTOR(15 downto 0);
           RD2 : out STD_LOGIC_VECTOR(15 downto 0);
           Ext_Imm : out STD_LOGIC_VECTOR(15 downto 0);
           func : out STD_LOGIC_VECTOR(2 downto 0);
           sa : out STD_LOGIC;
           WA: in std_logic_vector(2 downto 0);
           rd: out std_logic_vector(2 downto 0);
           rt: out std_logic_vector(2 downto 0)
           );
end component;

component UnitControl
    Port ( Instr : in STD_LOGIC_VECTOR(2 downto 0);
           RegDst : out STD_LOGIC;
           ExtOp : out STD_LOGIC;
           ALUSrc : out STD_LOGIC;
           Branch : out STD_LOGIC;
           Jump : out STD_LOGIC;
           ALUOp : out STD_LOGIC_VECTOR(2 downto 0);
           MemWrite : out STD_LOGIC;
           MemtoReg : out STD_LOGIC;
           RegWrite : out STD_LOGIC);
end component;

component ExecutionUnit is
    Port ( PCinc : in STD_LOGIC_VECTOR(15 downto 0);
           RD1 : in STD_LOGIC_VECTOR(15 downto 0);
           RD2 : in STD_LOGIC_VECTOR(15 downto 0);
           Ext_Imm : in STD_LOGIC_VECTOR(15 downto 0);
           func : in STD_LOGIC_VECTOR(2 downto 0);
           sa : in STD_LOGIC;
           ALUSrc : in STD_LOGIC;
           ALUOp : in STD_LOGIC_VECTOR(2 downto 0);
           BranchAddress : out STD_LOGIC_VECTOR(15 downto 0);
           ALURes : out STD_LOGIC_VECTOR(15 downto 0);
           Zero : out STD_LOGIC;
           rd: in std_logic_vector(2 downto 0);
           rt: in std_logic_vector(2 downto 0);
           RegDst: in std_logic;
           rWa: out std_logic_vector(2 downto 0)
           );
end component;

component DataMemory
    port ( clk : in STD_LOGIC;
           en : in STD_LOGIC;
           ALUResIn : in STD_LOGIC_VECTOR(15 downto 0);
           RD2 : in STD_LOGIC_VECTOR(15 downto 0);
           MemWrite : in STD_LOGIC;			
           MemData : out STD_LOGIC_VECTOR(15 downto 0);
           ALUResOut : out STD_LOGIC_VECTOR(15 downto 0));
end component;

signal Instruction, PCinc, RD1, RD2, WD, Ext_imm : STD_LOGIC_VECTOR(15 downto 0); 
signal JumpAddress, BranchAddress, ALURes, ALURes1, MemData : STD_LOGIC_VECTOR(15 downto 0);
signal func : STD_LOGIC_VECTOR(2 downto 0);
signal sa, zero : STD_LOGIC;
signal digits : STD_LOGIC_VECTOR(15 downto 0);
signal en, rst, PCSrc : STD_LOGIC; 
-- main controls 
signal RegDst, ExtOp, ALUSrc, Branch, Jump, MemWrite, MemtoReg, RegWrite : STD_LOGIC;
signal ALUOp :  STD_LOGIC_VECTOR(2 downto 0);

signal Inst_IF_ID: std_logic_vector(15 downto 0);
signal PCinc_IF_ID: std_logic_vector(15 downto 0);

signal RegDst_ID_EX: std_logic;
signal ALUSrc_ID_EX: std_logic;
signal Branch_ID_EX: std_logic;
signal MemWrite_ID_EX: std_logic;
signal MemToReg_ID_EX: std_logic;
signal RegWrite_ID_EX: std_logic;
signal ALUOp_ID_EX: std_logic_vector(2 downto 0);
signal RD1_ID_EX: std_logic_vector(15 downto 0);
signal RD2_ID_EX: std_logic_vector(15 downto 0);
signal Ext_Imm_ID_EX: std_logic_vector(15 downto 0);
signal func_ID_EX: std_logic_vector(2 downto 0);
signal sa_ID_EX: std_logic;
signal rd_ID_EX: std_logic_vector(2 downto 0);
signal rt_ID_EX: std_logic_vector(2 downto 0);
signal PCinc_ID_EX: std_logic_vector(15 downto 0);

signal Branch_EX_MEM: std_logic;
signal MemWrite_EX_MEM: std_logic;
signal MemToReg_EX_MEM: std_logic;
signal RegWrite_EX_MEM: std_logic;
signal Zero_EX_MEM: std_logic;
signal Branch_Address_EX_MEM: std_logic_vector(15 downto 0);
signal ALURes_EX_MEM: std_logic_vector(15 downto 0);
signal rd_EX_MEM: std_logic_vector(2 downto 0);
signal RD2_EX_MEM: std_logic_vector(15 downto 0);

signal MemToReg_MEM_WB: std_logic;
signal RegWrite_MEM_WB: std_logic;
signal ALUResOut_MEM_WB: std_logic_vector(15 downto 0);
signal MemData_MEM_WB: std_logic_vector(15 downto 0);
signal rd_MEM_WB: std_logic_vector(2 downto 0);

signal rd: std_logic_vector(2 downto 0);
signal rt: std_logic_vector(2 downto 0);
signal rWa: std_logic_vector(2 downto 0);

begin

    en_btn_0: MPG port map(en, btn(0), clk);
    rst_btn_1: MPG port map(rst, btn(1), clk);
    display : SSD port map (clk, digits, an, cat);
    
    -- controls
    InstrFetch: IFetch port map(clk, rst, en, Branch_Address_EX_MEM, JumpAddress, Jump, PCSrc, Instruction, PCinc);
    InstrDecode: IDecode port map(clk, en, Inst_IF_ID(12 downto 0), WD, RegWrite_MEM_WB, ExtOp, RD1, RD2, Ext_imm, func, sa, rd_MEM_WB, rd, rt);
    UControl: UnitControl port map(Inst_IF_ID(15 downto 13), RegDst, ExtOp, ALUSrc, Branch, Jump, ALUOp, MemWrite, MemtoReg, RegWrite);
    ExecUnit: ExecutionUnit port map(PCinc_ID_EX, RD1_ID_EX, RD2_ID_EX, Ext_imm_ID_EX, func_ID_EX, sa_ID_EX, ALUSrc_ID_EX, ALUOp_ID_EX, BranchAddress, ALURes, Zero, rd_ID_EX, rt_ID_EX, RegDst_ID_EX, rWa); 
    DataMem: DataMemory port map(clk, en, ALURes_EX_MEM, RD2_EX_MEM, MemWrite_EX_MEM, MemData, ALURes1);
    
    led(10 downto 0) <= ALUOp & ALUSrc & ExtOp & MemWrite & MemtoReg & RegWrite & RegDst & Branch & Jump;

    process(clk)
    begin
        if rising_edge(clk) and en = '1' then
            Inst_IF_ID <= Instruction;
            PCinc_IF_ID <= PCinc;
            
            RegDst_ID_EX <= RegDst;
            ALUSrc_ID_EX <= ALUSrc;
            Branch_ID_EX <= Branch;
            MemWrite_ID_EX <= MemWrite;
            MemToReg_ID_EX <= MemToReg;
            RegWrite_ID_EX <= RegWrite;
            ALUOp_ID_EX <= ALUOp;
            RD1_ID_EX <= RD1;
            RD2_ID_EX <= RD2;
            Ext_Imm_ID_EX <= Ext_Imm;
            func_ID_EX <= func;
            sa_ID_EX <= sa;
            rd_ID_EX <= rd;
            rt_ID_EX <= rt;
            PCinc_ID_EX <= PCinc_IF_ID;
            
            Branch_EX_MEM <= Branch_ID_EX;
            MemWrite_EX_MEM <= MemWrite_ID_EX;
            MemToReg_EX_MEM <= MemToReg_ID_EX;
            RegWrite_EX_MEM <= RegWrite_ID_EX;
            Zero_EX_MEM <= Zero;
            Branch_Address_EX_MEM <= BranchAddress;
            ALURes_EX_MEM <= ALURes;
            rd_EX_MEM <= rWa;
            RD2_EX_MEM <= RD2_ID_EX;
            
            MemToReg_MEM_WB <= MemToReg_EX_MEM;
            RegWrite_MEM_WB <= RegWrite_EX_MEM;
            ALUResOut_MEM_WB <= ALURes1;
            MemData_MEM_WB <= MemData;
            rd_MEM_WB <= rd_EX_MEM;
        end if;
    end process;

    JumpAddress <= PCinc_IF_ID(15 downto 13) & Inst_IF_ID(12 downto 0);

    PCSrc <= Zero_EX_MEM and Branch_EX_MEM;

    with MemtoReg_MEM_WB select
        WD <= MemData_MEM_WB when '1',
              ALUResOut_MEM_WB when '0',
              (others => '0') when others;

    with sw(7 downto 5) select
        digits <=  Instruction when "000", 
                   PCinc when "001",
                   RD1_ID_EX when "010",
                   RD2_ID_EX when "011",
                   Ext_Imm_ID_EX when "100",
                   ALURes when "101",
                   MemData when "110",
                   WD when "111",
                   (others => '0') when others; 
    
end Behavioral;