class Solution:
    def intToRoman(num: int) -> str:
        int_rom = {
            1: 'I',
            5: 'V',
            10: 'X',
            50: 'L',
            100: 'C',
            500: 'D',
            1000: 'M'
        }

        div = 1
        rom_rep = ''

        while num > 0 :
            digit = num%10
            if(digit < 4):
                rom_rep = digit * int_rom[div] + rom_rep
            elif(digit == 4):
                rom_rep = int_rom[div] + int_rom[div*5] + rom_rep
            elif(digit == 9):
                rom_rep = int_rom[div] + int_rom[div*10] + rom_rep
            else :
                rom_rep = int_rom[div*5] + int_rom[div] * (digit-5) + rom_rep

            div *=10
            num = num//10

        return rom_rep
    