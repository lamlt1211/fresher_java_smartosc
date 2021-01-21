package com.smartosc.training.exception;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class CreatePostReq {
//    @NotNull(message = "CategoryID is required")
    private int categoryID;

//    @NotNull(message = "UserID is required")
    private int userID;

    @NotNull(message = "Image is required")
    @NotEmpty(message = "Image must be not Empty")
    private String img;

    @NotNull(message = "Title is required")
    @NotEmpty(message = "Title must be not Empty")
    private String title;

    @NotNull(message = "Content is required")
    @NotEmpty(message = "Content must be not Empty")
    private String content;
}
