package org.destirec.destirec.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.springframework.lang.NonNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResponseData<T>{

    @Nullable
    private T data;
    @Nullable
    private String error;
    public ResponseData(@NonNull T data) {
        this.data = data;
    }

    public ResponseData(@NonNull String error) {
        this.error = error;
    }
}
