package com.smartosc.training.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * fres-parent
 *
 * @author Namtt
 * @created_at 20/04/2020 - 5:58 PM
 * @created_by Namtt
 * @since 20/04/2020
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AbstractDTO implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Date createdAt;
    private Date updatedAt;
    private int status;
}
