fun main(args: Array<String>) {
    val nf = NetworkFeystel()

//    print("Введите значение для шифрования: ")
//    val msg = readln().toLong()
//
//    val s_msg = nf.shifr(msg)
//    println("Зашифрованное сообщение: $s_msg")
//    val msg_ = nf.rasshifr(s_msg)
//    println("Расшифрованное сообщение: $msg_")

//    val message = readln()
//    val list = mutableListOf<Long>()
//    for(char in message) {
//        list.add(nf.shifr(char.code.toLong()))
//    }
//    println("_________________________________________________________")
//    val listRasshifr = mutableListOf<Char>()
//    for(number in list) {
//        println(number)
//        listRasshifr.add(nf.rasshifr(number).toInt().toChar())
//    }
//
//    val msg_ = listRasshifr.toCharArray().joinToString("")
//    println("Расшифрованное сообщение: $msg_")
    nf.lab2()
}