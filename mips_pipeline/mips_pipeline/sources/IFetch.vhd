library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

entity IFetch is
    Port (clk: in STD_LOGIC;
          rst : in STD_LOGIC;
          en : in STD_LOGIC;
          BranchAddress : in STD_LOGIC_VECTOR(15 downto 0);
          JumpAddress : in STD_LOGIC_VECTOR(15 downto 0);
          Jump : in STD_LOGIC;
          PCSrc : in STD_LOGIC;
          Instruction : out STD_LOGIC_VECTOR(15 downto 0);
          PCinc : out STD_LOGIC_VECTOR(15 downto 0));
end IFetch;

architecture Behavioral of IFetch is

type tROM is array(0 to 255) of std_logic_vector(15 downto 0);
signal ROM: tROM := ( 
B"010_000_010_0000000",		--li $2, 0		0	0
B"010_010_001_0000000",		--lw $1, length	1	1
X"0000",				--nop			2	10
B"001_010_010_0000001",		--addi $2, $2, 1	3	11

--L1:
B"100_010_001_1000010",		--beq $2, $1, L8	4	100
X"0000",				--nop			5	101
X"0000",				--nop			6	110
X"0000",				--nop			7	111
B"010_010_100_0000000",		--lw $4, 0($3)	8	1000
X"0000",				--nop			9	1001
X"0000",				--nop			10	1010
B"000_000_101_100_0_011",	--srl $5, $4, 0	11	1011
X"0000",				--nop			12	1100
X"0000",				--nop			13	1101
B"100_101_000_0011111",	--beq $5, $0, L2	14	1110
X"0000",				--nop			15	1111
X"0000",				--nop			16	10000
X"0000",				--nop			17	10001
B"010_000_110_0000100",		--lw $6, maxImpar	18	10010
X"0000",				--nop			19	10011
X"0000",				--nop			20	10100
B"000_100_110_111_0_111",	--slt $7, $4, $6	21	10101
X"0000",				--nop			22	10110
X"0000",				--nop			23	10111
B"100_111_000_0100011",		--beq &7, $0, L3	24	11000
X"0000",				--nop			25	11001
X"0000",				--nop			26	11010
X"0000",				--nop			27	11011
B"011_110_100_0000000",		--sw $4, maxImpar	28	11100
B"111_0000000100000",		--j L4		29	11101
X"0000",				--nop			30	11110

--L2:
B"000_111_100_111_0_000",	--add $7, $7, $4	31	11111

--L4:
B"001_010_010_0000001",		--addi $2, $2, 1	32	100000
B"111_0000000000100", 		--j L1		33	100001
X"0000",				--nop			34	100010

--L3:
B"010_000_110_0000101",		--lw $6, sum	35	100011
B"010_000_101_0000100",		--lw $5, maxImpar	36	100100
X"0000",				--nop			37	100101
X"0000",				--nop			38	100110
B"000_110_101_111_0_111",	--slt $7, $6, $5	39	100111
X"0000",				--nop			40	101000
X"0000",				--nop			41	101001
B"100_111_000_0010110",		--beq $7, $0, L5	42	101010
X"0000",				--nop			43	101011
X"0000",				--nop			44	101100
X"0000",				--nop			45	101101
B"010_000_111_0000011",		--li $7, 2		46	101110
X"0000",				--nop			47	101111
X"0000",				--nop			48	110000
B"011_110_100_0000000",		--sw $7, result	49	110001
B"111_0000001000010",		--j L8		50	110010
X"0000",				--nop			51	110011
		
--L5:
B"100_110_101_0111110",		--beq $6, $5, L6	52	110100
X"0000",				--nop			53	110101
X"0000",				--nop			54	110110
X"0000",				--nop			55	110111
B"010_000_111_0000001",		--li &7, 0		56	111000
X"0000",				--nop			57	111001
X"0000",				--nop			58	111010
B"011_110_100_0000000",		--sw $7, result	59	111011
B"111_0000001000010",		--j L8		60	111100
X"0000",				--nop			61	111101

--L6:			
B"010_000_111_0000010",		--li $7, 1		62	111110
X"0000",				--nop			63	111111
X"0000",				--nop			64	1000000
B"011_110_100_0000000",		--sw $7, 1		65	1000001

--L8:
others => X"0000");			--nop			66	1000010                                                        28 --b11100 --X"0000"

signal PC : STD_LOGIC_VECTOR(15 downto 0) := (others => '0');
signal PCAux, NextAddr, AuxSgn, AuxSgn1: STD_LOGIC_VECTOR(15 downto 0);

begin
    Instruction <= ROM(conv_integer(PC(7 downto 0)));

    PCAux <= PC + 1;
    PCinc <= PCAux;

    process(clk)
    begin
        if rising_edge(clk) then
            if rst = '1' then
                PC <= (others => '0');
            elsif en = '1' then
                PC <= NextAddr;
            end if;
        end if;
    end process;

    process(PCSrc, PCAux, BranchAddress) begin
        case PCSrc is 
            when '1' => AuxSgn <= BranchAddress;
            when others => AuxSgn <= PCAux;
        end case;
    end process;	

    process(Jump, AuxSgn, JumpAddress) begin
        case Jump is
            when '1' => NextAddr <= JumpAddress;
            when others => NextAddr <= AuxSgn;
        end case;
    end process;

end Behavioral;