package com.example.entity.vo.response;

import com.example.entity.dto.Account;
import com.example.entity.dto.UserNFTDto;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class UserNFTVO {

    UserNFTDto userNFTDto;
    Account account;

}
