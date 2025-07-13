package com.example.service.impl.sensor;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.RestBean;
import com.example.entity.dto.SensorDataInfoDto;
import com.example.entity.dto.SensorInfoDto;
import com.example.mapper.sensor.SensorDataInfoMapper;
import com.example.service.AccountService;
import com.example.service.blockchain.MessageReportService;
import com.example.service.sensor.SensorDataInfoAddService;
import com.example.service.sensor.SensorInfoSelectService;
import com.example.utils.BlockchainHashUtil;
import com.example.utils.Const;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SensorDataInfoAddImpl extends ServiceImpl<SensorDataInfoMapper, SensorDataInfoDto>
        implements SensorDataInfoAddService {
    @Resource
    JwtUtils jwtUtils;

    @Resource
    BlockchainHashUtil blockchainHashUtil;

    @Resource
    SensorInfoSelectService selectService;

    @Resource
    MessageReportService messageReportService;

    @Resource
    AccountService accountService;

    @Override
    public <T> RestBean<T> addSensorDataInfoSingle(HttpServletRequest request, SensorDataInfoDto dto) throws Exception {

        if(!this.SensorFarmerIdVerify(request))return RestBean.failure(401,"权限不足");
        SensorInfoDto infoDto = selectService.getSensorInfoBySensorId(dto.getSensorId());
        Integer fid = infoDto.getFarmId();
        if(fid==null) return RestBean.failure(500, "没有该编号的传感器");
        if (!jwtUtils.getUserIdVerify(request,fid)) return RestBean.failure(500, "请检查传感器所属农户");
        dto.setBlockchain_hash(blockchainHashUtil.generateSensorHash(dto));
        String userAddress = this.accountService.findAccountById(fid).getWalletAddress();
        if(this.save(dto)){
            /** (已完成)
             * 这里要执行上链操作 ----->  blockChainEvidenceService
             * 如果信息存储成功--->生成区块链凭证初始信息--->调用合约进行上链操作
             *      --->区块链返回上链成功的区块号--->更新数据库的上链信息。
             */
            List<Object> params = new ArrayList<>();
            params.add(0,3);
            params.add(1,dto.getDataId());
            params.add(2,dto.getBlockchain_hash());
            this.messageReportService.blockChainEvidenceReport(Const.CONTRACT_FOR_MESSAGE_REPORT_METHOD_ADD_EVIDENCE
                    ,params
                    ,userAddress,Const.CONTRACT_FOR_MESSAGE_REPORT);
            return RestBean.success();
        }

       return RestBean.failure(500,"内部错误请联系管理员");
    }

    @Override
    public <T> RestBean<T> addSensorDataInfoMutil(HttpServletRequest request, List<SensorDataInfoDto> dto) {
        if(!this.SensorFarmerIdVerify(request))return RestBean.failure(401,"权限不足");
        for (SensorDataInfoDto sensorData : dto) {
            SensorInfoDto infoDto = selectService.getSensorInfoBySensorId(sensorData.getSensorId());
            Integer fid = infoDto.getFarmId();
            if(fid==null) return RestBean.failure(500, "没有该编号的传感器");
            if (!jwtUtils.getUserIdVerify(request,fid)) return RestBean.failure(500, "不准增加其他农户传感器信息");
            sensorData.setBlockchain_hash(blockchainHashUtil.generateSensorHash(sensorData));
            if (!this.save(sensorData)) return RestBean.failure(500, "添加有误，操作终止");
        }


        /**
         * 这里要执行上链操作 ----->  blockChainEvidenceService
         * 如果信息存储成功--->生成区块链凭证初始信息--->调用合约进行上链操作
         *      --->区块链返回上链成功的区块号--->更新数据库的上链信息。
         */
        return RestBean.success();
    }





    private  boolean SensorFarmerIdVerify (HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        DecodedJWT jwt = jwtUtils.resolveJWT(authorization);
        String role = jwtUtils.toRole(jwt);
        return !role.equals("2");
    }

}
