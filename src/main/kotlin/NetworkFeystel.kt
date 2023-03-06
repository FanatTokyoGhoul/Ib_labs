class NetworkFeystel(
    private val N: Int = 8,
    private val size_K: Int = 64,
    private val K: Long = 0x96EA704CFB1CF67,
    private val msgList: List<Long> = listOf(
        0x123456789ABCDEF0,
        0x123456789ABCDEF0,
        0x1FBA85C953ABCFD0,
        0x1FBA85C953ABCFD0,
        0x123456789ABCDEF0,
        0x123456789ABCDEF0,
        0x1FBA85C953ABCFD0,
        0x1FBA85C953ABCFD0,
        0x123456789ABCDEF0,
        0x123456789ABCDEF0,
        0x1FBA85C953ABCFD0,
        0x1FBA85C953ABCFD0,
    ),
    private val IV: Long = 0x18FD47203C7A23BC,
) {
    private val F32: Int = -1

    private fun shift32(x: Int, t: Int, direction: ShiftDirection): Int {
        return if (direction == ShiftDirection.RIGHT) {
            ((x shr t) or (x shl (32 - t)))
        } else {
            ((x shl t) or (x shr (32 - t)))
        }
    }

    private fun shift64(x: Long, t: Int, direction: ShiftDirection): Long {
        return if (direction == ShiftDirection.RIGHT) {
            ((x shr t) or (x shl (64 - t)))
        } else {
            ((x shl t) or (x shr (64 - t)))
        }
    }

    private fun ki(i: Int): Int {
        return shift64(K, i * 8, ShiftDirection.RIGHT).toInt()
    }

    private fun F(polblok: Int, K_i: Int): Int {
        val f1 = shift32(polblok, 9, ShiftDirection.LEFT)
        val f2 = shift32(K_i, 11, ShiftDirection.RIGHT) or polblok
        return f1 xor f2
    }

    fun shifr(block: Long): Long {
        var left_b = ((block shr 32) and F32.toLong()).toInt()
        var right_b = (block and F32.toLong()).toInt()


        for (i in 0 until N) {
            val K_i = ki(i)

            val left_i = left_b
            val right_i = right_b xor F(left_b, K_i)

            //println("in $i left = $left_b; right = $right_b")

            if (i < N - 1) {
                left_b = right_i
                right_b = left_i
            } else {
                left_b = left_i
                right_b = right_i
            }

            //println("out $i left = $left_b; right = $right_b")
        }

        var shifroblok = left_b.toLong()
        shifroblok = (shifroblok shl 32) or (right_b and F32)
        return shifroblok
    }

    fun rasshifr(block: Long): Long {
        var left_b = ((block shr 32) and F32.toLong()).toInt()
        var right_b = (block and F32.toLong()).toInt()

        for (i in N - 1 downTo 0 step 1) {
            val K_i = ki(i)
            val left_i = left_b
            val right_i = right_b xor F(left_b, K_i)

            //println("in $i left = $left_b; right = $right_b")

            if (i > 0) {
                left_b = right_i
                right_b = left_i
            } else {
                left_b = left_i
                right_b = right_i
            }

            //println("out $i left = $left_b; right = $right_b")
        }

        var shifroblok = left_b.toLong()
        shifroblok = (shifroblok shl 32) or (right_b and F32)
        return shifroblok
    }

    fun lab2() {
        println("Init Key $K")
        println("Init V $IV")
        println("Text ${msgList.map { it.toInt().toChar() }.toCharArray().joinToString("")}")

        val msg_ecb = mutableListOf<Long>()
        println("Shifr ECB:")

        msgList.map {
            msg_ecb.add(shifr(it))
            println(it)
        }

        val msg_cbc = mutableListOf<Long>()
        println("Shifr CBC:")

        var blok = msgList[0] xor IV
        msg_cbc.add(shifr(blok))
        println(msg_cbc[0])
        for(b in 1 until msgList.size) {
            blok = msgList[b] xor msg_cbc[b - 1]
            msg_cbc.add(shifr(blok))
            println(msg_cbc[b])
        }

        val msg_ofb = mutableListOf<Long>()
        println("Shifr OFB:")
        blok = IV

        for(b in msgList.indices) {
            blok = shifr(blok)
            msg_ofb.add(blok xor msgList[b])
            println(msg_ofb[b])
        }

        println("Text ECB:")
        print(msg_ecb.map { rasshifr(it).toInt().toChar() }.toCharArray().joinToString("") )
        println()

        println("Text CBC:")
        var msg_b = rasshifr(msg_cbc[0])
        msg_b = msg_b xor IV
        print(msg_b.toInt().toChar())
        for(b in 1 until msgList.size) {
            msg_b = rasshifr(msg_cbc[b])
            msg_b = msg_b xor msg_cbc[b - 1]
            print(msg_b.toInt().toChar())
        }
        println()

        println("Text OFB:")

        blok = IV

        for(b in msgList.indices) {
            blok = shifr(blok)
            msg_b = blok xor msg_ofb[b]
            print(msg_b.toInt().toChar())
        }
    }

    private enum class ShiftDirection {
        RIGHT, LEFT
    }
}

private infix fun Long.or(intNumber: Int): Long {
    var number: Long = 0L
    for (i in 0..30) {
        number = (intNumber and (1 shl i)).toLong() or (this and (1L shl i)) or number
    }
    if (intNumber < 0 || (1L shl 31) and this == (1L shl 31)) number = number or (1L shl 31)
    return number or this
}
//public static void Main(string[] args)
//{
//    // Исходное сообщение
//    Console.WriteLine("{0:X}", msg);
//
//    // Зашифрованное сообщение
//    UInt64 c_msg = shifr(msg);
//    Console.WriteLine("{0:X}", c_msg);
//
//    // Расшифрованное сообщение
//    UInt64 msg_ = rasshifr(c_msg);
//    Console.WriteLine("{0:X}", msg_);
//
//
//
//    // Отображаем на консоли исходный ключ K (и IV) для зашифровки и исходное сообщение text (все это объявлено в первых строках)
//    Console.WriteLine("Init Key {0:X}", K); 	// большой ключ K (64 бит) из битов которого создаются маленькие ключи K_i (по 32 бита)
//
//    Console.WriteLine("Init V {0:X}", IV); 	 	// дополнительный ключ (вектор) для шифровки первого блока сообщения врежимах CBC и OFB (64 бит)
//
//    // Вывод блоков сообщения до шифрования
//    Console.WriteLine("Text (message blocks)");
//    for (int b = 0; b < B; b++)
//    Console.Write("{0:X} ", msg[b]);	// выводим очередной блок сообщения
//
//    // 1. Шифрование
//
//    // 1.1. Шифрование в режиме ECB (электронная кодовая книга)
//    UInt64[] msg_ecb = new UInt64[B];
//    Console.WriteLine("\nShifr ECB:");
//
//    // Шифрование последовательно каждого блока без дополнительных преобразований
//    for (int b = 0; b < B; b++)
//    {
//        msg_ecb[b] = shifr(msg[b]);		// шифруем блок
//        Console.Write("{0:X} ", msg_ecb[b]);	// выводим очередной блок сообщения	// выводим зашифрованный блок на консоль
//        // В зашифрованном тексте 1й и 2й блоки одинаковы (3й с 4м тоже) как и в исходном сообщении - это недостаток режима ECB
//    }
//
//    // 1.2. Шифрование в режиме CBC (режим сцепления блоков шифротекста)
//    UInt64[] msg_cbc = new UInt64[B];
//    Console.WriteLine("\nShifr CBC:");
//    // Первый блок сообщения xor'ится с IV перед шифрованием:
//    UInt64 blok = msg[0] ^ IV;
//    msg_cbc[0] = shifr(blok); // шифруем блок
//    Console.Write("{0:X} ", msg_cbc[0]);	// выводим зашифрованный первый блок на консоль
//
//    // Каждый последующий блок перед шифрованием xor'ится с предыдущим зашифрованным блоком:
//    for (int b = 1; b < B; b++)
//    {
//        blok = msg[b] ^ msg_cbc[b - 1]; // xor с предыдущим зашифрованным
//        msg_cbc[b] = shifr(blok); // шифруем блок
//        Console.Write("{0:X} ", msg_cbc[b]);	// выводим зашифрованный блок на консоль
//        // В зашифрованном тексте все блоки будут разными, не смотря на то что в исходном сообщении они повторялись
//    }
//
//    // 1.3. Шифрование в режиме OFB (режим обратной связи по выходу)
//    UInt64[] msg_ofb = new UInt64[B];
//    Console.WriteLine("\nShifr OFB:");
//    blok = IV; // дополнительный ключ для зашифровки блоков текста
//
//    for (int b = 0; b < B; b++)
//    {
//        blok = shifr(blok);	// на каждом шаге шифруется этот дополнительный ключ
//        msg_ofb[b] = blok ^ msg[b]; // и xor'ится с очередным блоком сообщения - получается зашифрованный блок сообщения
//        Console.Write("{0:X} ", msg_ofb[b]);	// выводим зашифрованный блок на консоль
//        // В зашифрованном тексте все блоки будут разными, не смотря на то что в исходном сообщении они повторялись
//    }
//
//    // 2. Расшифрование
//    // 2.1. Расшифровка в режиме ECB (электронная кодовая книга)
//    UInt64 msg_b; 	// блок расшифрованного текста
//    Console.WriteLine("\nText ECB:");
//    // Расшифровка последовательно каждого блока без дополнительных преобразований
//    for (int b = 0; b < B; b++)
//    {
//        msg_b = rasshifr(msg_ecb[b]); 	// расшифровка блока
//        Console.Write("{0:X} ", msg_b); 	// выводим расшифрованный блок на консоль
//    }
//
//    // 2.2. Расшифровка в режиме CBC (режим сцепления блоков шифротекста)
//    Console.WriteLine("\nText CBC:");
//    // Первый блок сообщения xor'ится с IV после расшифровки:
//    msg_b = rasshifr(msg_cbc[0]); 	// расшифровка блока
//    msg_b ^= IV; // xor'им с IV после расшифровки
//    Console.Write("{0:X} ", msg_b);	// выводим расшифрованный первый блок на консоль
//    // Каждый последующий блок после расшифровки xor'ится с предыдущим зашифрованным блоком:
//    for (int b = 1; b < B; b++)
//    {
//        msg_b = rasshifr(msg_cbc[b]);	// расшифровка блока
//        msg_b ^= msg_cbc[b - 1]; 		// xor с предыдущим зашифрованным
//        Console.Write("{0:X} ", msg_b);	// выводим расшифрованный блок на консоль
//    }
//
//    // 2.3. Расшифровка в режиме OFB (режим обратной связи по выходу)
//    Console.WriteLine("\nText OFB:");
//    blok = IV; // дополнительный ключ для расшифровки блоков текста
//    for (int b = 0; b < B; b++)
//    {
//        blok = shifr(blok);	// на каждом шаге шифруется этот дополнительный ключ (точно так же как при шифровании)
//        msg_b = blok ^ msg_ofb[b];	// расшифрованный блок сообщения получается в результате операции xor зашифрованного блока сообщения и этого ключа
//        Console.Write("{0:X} ", msg_b);	// выводим расшифрованный блок на консоль
//    }
//}
//}