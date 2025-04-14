library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

entity DataMemory is
    port ( clk : in STD_LOGIC;
           en : in STD_LOGIC;
           ALUResIn : in STD_LOGIC_VECTOR(15 downto 0);
           RD2 : in STD_LOGIC_VECTOR(15 downto 0);
           MemWrite : in STD_LOGIC;			
           MemData : out STD_LOGIC_VECTOR(15 downto 0);
           ALUResOut : out STD_LOGIC_VECTOR(15 downto 0));
end DataMemory;

architecture Behavioral of DataMemory is

type mem_type is array (0 to 31) of STD_LOGIC_VECTOR(15 downto 0);
signal MEM : mem_type := (
    X"0007",    --offset for i
    X"0000",    --imm 0
    X"0001",    --imm 1
    X"0002",    --imm 2
    X"0000",    --maxImpar
    X"0000",    --sum
    X"0000",    --result
    X"0008",    --length
    X"0003",    --a[0]
    X"0005",    --a[1]
    X"0006",    --a[2]
    X"0002",    --a[3]
    X"0001",    --a[4]
    X"0004",    --a[5]
    X"000F",    --a[6]
    X"0002",    --a[7]
    
    others => X"0000");

begin

    MemData <= MEM(conv_integer(ALUResIn(4 downto 0)));
    ALUResOut <= ALUResIn;

    process(clk) begin
        if rising_edge(clk) then
            if en = '1' and MemWrite='1' then
                MEM(conv_integer(ALUResIn(4 downto 0))) <= RD2;			
            end if;
        end if;
    end process;

end Behavioral;