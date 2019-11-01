package com.tencent.devops.store.pojo.image.response

import com.tencent.devops.common.pipeline.type.docker.ImageType
import com.tencent.devops.store.pojo.common.Category
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("OP镜像详情")
data class OpImageItem(
    @ApiModelProperty("镜像ID", required = true)
    val imageId: String,
    @ApiModelProperty("镜像代码", required = true)
    val imageCode: String,
    @ApiModelProperty("镜像名称", required = true)
    val imageName: String,
    @ApiModelProperty("镜像类型，BKDEVOPS：蓝盾  BKSTORE：研发商店  THIRD：第三方开发", required = false)
    val imageType: ImageType?,

    @ApiModelProperty("版本号", required = true)
    val imageVersion: String,
    @ApiModelProperty(
        "镜像状态，INIT：初始化|COMMITTING：提交中|CHECKING：验证中|CHECK_FAIL：验证失败|TESTING：测试中|AUDITING：审核中|AUDIT_REJECT：审核驳回|RELEASED：已发布|GROUNDING_SUSPENSION：上架中止|UNDERCARRIAGING：下架中|UNDERCARRIAGED：已下架",
        required = false
    )
    val imageStatus: String,
    @ApiModelProperty("需管理员操作的最新镜像ID", required = false)
    var opImageId: String? = null,
    @ApiModelProperty("需管理员操作的最新镜像版本号", required = false)
    var opImageVersion: String? = null,
    @ApiModelProperty(
        "需管理员操作的最新镜像状态，INIT：初始化|COMMITTING：提交中|CHECKING：验证中|CHECK_FAIL：验证失败|TESTING：测试中|AUDITING：审核中|AUDIT_REJECT：审核驳回|RELEASED：已发布|GROUNDING_SUSPENSION：上架中止|UNDERCARRIAGING：下架中|UNDERCARRIAGED：已下架",
        required = false
    )
    var opImageStatus: String? = null,
    @ApiModelProperty("所属分类代码", required = false)
    val classifyCode: String?,
    @ApiModelProperty("所属分类名称", required = false)
    val classifyName: String?,
    @ApiModelProperty("范畴列表", required = false)
    val categoryList: List<Category>?,

    @ApiModelProperty("发布者", required = true)
    val publisher: String?,

    @ApiModelProperty("发布时间", required = false)
    val pubTime: Long?,

    @ApiModelProperty("发布描述", required = false)
    val pubDescription: String?,

    @ApiModelProperty("是否为最新版本镜像 true：最新 false：非最新", required = true)
    val latestFlag: Boolean,

    @ApiModelProperty("是否为公共镜像 true：公共镜像 false：普通镜像", required = false)
    val publicFlag: Boolean?,

    @ApiModelProperty("是否推荐， TRUE：是 FALSE：不是", required = false)
    val recommendFlag: Boolean?,
    @ApiModelProperty("权重（数值越大代表权重越高）", required = false)
    val weight: Int?,
    @ApiModelProperty("创建人")
    val creator: String?,
    @ApiModelProperty("修改人")
    val modifier: String?,
    @ApiModelProperty("创建时间")
    val createTime: Long,
    @ApiModelProperty("修改时间")
    val updateTime: Long
)