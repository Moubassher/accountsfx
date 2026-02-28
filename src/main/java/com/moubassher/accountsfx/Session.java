package com.moubassher.accountsfx;

import java.time.LocalDateTime;

public record Session(int userId, LocalDateTime login, LocalDateTime logout) {


}
