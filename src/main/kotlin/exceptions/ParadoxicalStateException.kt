package exceptions

class ParadoxicalStateException(message: String, normal: String) : RuntimeException("$message ($normal)")