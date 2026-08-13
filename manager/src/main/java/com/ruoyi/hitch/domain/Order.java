package com.ruoyi.hitch.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 订单对象 t_order
 * 
 * @author Shawn
 * @date 2026-08-13
 */
public class Order extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 乘客ID */
    @Excel(name = "乘客ID")
    private String passengerId;

    /** 乘客行程ID */
    @Excel(name = "乘客行程ID")
    private String passengerStrokeId;

    /** 司机ID */
    @Excel(name = "司机ID")
    private String driverId;

    /** 司机行程ID */
    @Excel(name = "司机行程ID")
    private String driverStrokeId;

    /** 米 */
    @Excel(name = "米")
    private Long distance;

    /** 秒 */
    @Excel(name = "秒")
    private Long estimatedTime;

    /** 价格 */
    @Excel(name = "价格")
    private Long cost;

    /** 状态 */
    @Excel(name = "状态")
    private Long status;

    /** 乐观锁 */
    @Excel(name = "乐观锁")
    private Long REVISION;

    /** 创建人 */
    @Excel(name = "创建人")
    private String createdBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createdTime;

    /** 更新人 */
    @Excel(name = "更新人")
    private String updatedBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date updatedTime;

    public void setId(String id) 
    {
        this.id = id;
    }

    public String getId() 
    {
        return id;
    }
    public void setPassengerId(String passengerId) 
    {
        this.passengerId = passengerId;
    }

    public String getPassengerId() 
    {
        return passengerId;
    }
    public void setPassengerStrokeId(String passengerStrokeId) 
    {
        this.passengerStrokeId = passengerStrokeId;
    }

    public String getPassengerStrokeId() 
    {
        return passengerStrokeId;
    }
    public void setDriverId(String driverId) 
    {
        this.driverId = driverId;
    }

    public String getDriverId() 
    {
        return driverId;
    }
    public void setDriverStrokeId(String driverStrokeId) 
    {
        this.driverStrokeId = driverStrokeId;
    }

    public String getDriverStrokeId() 
    {
        return driverStrokeId;
    }
    public void setDistance(Long distance) 
    {
        this.distance = distance;
    }

    public Long getDistance() 
    {
        return distance;
    }
    public void setEstimatedTime(Long estimatedTime) 
    {
        this.estimatedTime = estimatedTime;
    }

    public Long getEstimatedTime() 
    {
        return estimatedTime;
    }
    public void setCost(Long cost) 
    {
        this.cost = cost;
    }

    public Long getCost() 
    {
        return cost;
    }
    public void setStatus(Long status) 
    {
        this.status = status;
    }

    public Long getStatus() 
    {
        return status;
    }
    public void setREVISION(Long REVISION) 
    {
        this.REVISION = REVISION;
    }

    public Long getREVISION() 
    {
        return REVISION;
    }
    public void setCreatedBy(String createdBy) 
    {
        this.createdBy = createdBy;
    }

    public String getCreatedBy() 
    {
        return createdBy;
    }
    public void setCreatedTime(Date createdTime) 
    {
        this.createdTime = createdTime;
    }

    public Date getCreatedTime() 
    {
        return createdTime;
    }
    public void setUpdatedBy(String updatedBy) 
    {
        this.updatedBy = updatedBy;
    }

    public String getUpdatedBy() 
    {
        return updatedBy;
    }
    public void setUpdatedTime(Date updatedTime) 
    {
        this.updatedTime = updatedTime;
    }

    public Date getUpdatedTime() 
    {
        return updatedTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("passengerId", getPassengerId())
            .append("passengerStrokeId", getPassengerStrokeId())
            .append("driverId", getDriverId())
            .append("driverStrokeId", getDriverStrokeId())
            .append("distance", getDistance())
            .append("estimatedTime", getEstimatedTime())
            .append("cost", getCost())
            .append("status", getStatus())
            .append("REVISION", getREVISION())
            .append("createdBy", getCreatedBy())
            .append("createdTime", getCreatedTime())
            .append("updatedBy", getUpdatedBy())
            .append("updatedTime", getUpdatedTime())
            .toString();
    }
}
