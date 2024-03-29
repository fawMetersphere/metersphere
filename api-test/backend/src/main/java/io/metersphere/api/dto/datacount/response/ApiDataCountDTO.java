package io.metersphere.api.dto.datacount.response;

import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.base.domain.ApiDefinition;
import io.metersphere.commons.constants.RequestTypeConstants;
import io.metersphere.commons.enums.ApiHomeFilterEnum;
import io.metersphere.commons.enums.ApiReportStatus;
import io.metersphere.commons.enums.ApiTestDataStatus;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 接口数据统计返回
 */
@Getter
@Setter
public class ApiDataCountDTO {
    private long total = 0;
    private long httpCount = 0;
    private long tcpCount = 0;
    private long rpcCount = 0;
    private long sqlCount = 0;
    private long createdInWeek = 0;

    //不同请求方式的覆盖数
    private long httpCovered = 0;
    private long tcpCovered = 0;
    private long sqlCovered = 0;
    private long rpcCovered = 0;

    //不同请求方式的未覆盖
    private long httpNotCovered = 0;
    private long tcpNotCovered = 0;
    private long rpcNotCovered = 0;
    private long sqlNotCovered = 0;

    private long coveredCount = 0;
    private long notCoveredCount = 0;
    private long runningCount = 0;
    private long finishedCount = 0;
    private long notStartedCount = 0;
    //本周执行次数
    private long executedTimesInWeek = 0;
    //历史执行总次数
    private long executedTimes = 0;
    //执行的数据数
    private long executedCount = 0;
    private long notExecutedCount = 0;
    private long passCount = 0;
    private long unPassCount = 0;
    private long fakeErrorCount = 0;
    private long notRunCount = 0;

    //接口覆盖率
    private String apiCoveredRate = "0%";
    //完成率
    private String completedRate = "0%";
    //执行率
    private String executedRate = "0%";
    //通过率
    private String passRate = "0%";

    public void countProtocol(Map<String, List<ApiDefinition>> protocalAllApiMap) {
        for (Map.Entry<String, List<ApiDefinition>> entry : protocalAllApiMap.entrySet()) {
            switch (entry.getKey()) {
                case RequestTypeConstants.DUBBO:
                    this.rpcCount += entry.getValue().size();
                    break;
                case RequestTypeConstants.HTTP:
                    this.httpCount += entry.getValue().size();
                    break;
                case RequestTypeConstants.SQL:
                    this.sqlCount += entry.getValue().size();
                    break;
                case RequestTypeConstants.TCP:
                    this.tcpCount += entry.getValue().size();
                    break;
                default:
                    break;
            }
        }
        this.total = this.rpcCount + this.httpCount + this.sqlCount + this.tcpCount;
    }


    public void countProtocol(List<ApiDataCountResult> countResultList) {
        for (ApiDataCountResult countResult :
                countResultList) {
            switch (countResult.getGroupField().toUpperCase()) {
                case RequestTypeConstants.DUBBO:
                    this.rpcCount += countResult.getCountNumber();
                    break;
                case RequestTypeConstants.HTTP:
                    this.httpCount += countResult.getCountNumber();
                    break;
                case RequestTypeConstants.SQL:
                    this.sqlCount += countResult.getCountNumber();
                    break;
                case RequestTypeConstants.TCP:
                    this.tcpCount += countResult.getCountNumber();
                    break;
                default:
                    break;
            }
        }
        this.total = this.rpcCount + this.httpCount + this.sqlCount + this.tcpCount;
    }

    /**
     * 对Status视角对查询结果进行统计
     *
     * @param countResultList 查询参数
     */
    public void countStatus(List<ApiDataCountResult> countResultList) {
        for (ApiDataCountResult countResult :
                countResultList) {
            if (StringUtils.equalsIgnoreCase(countResult.getGroupField(), ApiTestDataStatus.UNDERWAY.getValue())) {
                //运行中
                this.runningCount += countResult.getCountNumber();
            } else if (StringUtils.equalsIgnoreCase(countResult.getGroupField(), ApiTestDataStatus.COMPLETED.getValue())) {
                //已完成
                this.finishedCount += countResult.getCountNumber();
            } else if (StringUtils.equalsIgnoreCase(countResult.getGroupField(), ApiTestDataStatus.PREPARE.getValue())) {
                this.notStartedCount += countResult.getCountNumber();
            }
        }
    }

    public void countApiCoverage(List<ApiDataCountResult> countResultList) {
        for (ApiDataCountResult countResult : countResultList) {
            if (StringUtils.equalsIgnoreCase(countResult.getGroupField(), ApiHomeFilterEnum.COVERED)) {
                this.coveredCount += countResult.getCountNumber();
            } else if (StringUtils.equalsIgnoreCase(countResult.getGroupField(), ApiHomeFilterEnum.NOT_COVERED)) {
                this.notCoveredCount += countResult.getCountNumber();
            }
        }
    }

    public void countScenarioRunResult(List<ApiDataCountResult> countResultByRunResult) {
        for (ApiDataCountResult countResult : countResultByRunResult) {
            if (StringUtils.equalsAnyIgnoreCase(countResult.getGroupField(), ApiHomeFilterEnum.NOT_RUN,
                    ApiReportStatus.PENDING.name()) || StringUtils.isEmpty(countResult.getGroupField())) {
                this.notExecutedCount += countResult.getCountNumber();
            } else if (ApiReportStatus.ERROR.name().equals(countResult.getGroupField())) {
                this.unPassCount += countResult.getCountNumber();
            } else if (StringUtils.equalsAnyIgnoreCase(countResult.getGroupField(), ApiReportStatus.FAKE_ERROR.name())) {
                this.fakeErrorCount += countResult.getCountNumber();
            } else {
                this.passCount += countResult.getCountNumber();
            }
        }
        this.executedCount = this.unPassCount + this.fakeErrorCount + this.passCount;
    }

    /**
     * 获取执行过的数据数
     *
     * @return
     */
    public long getExecutedData() {
        return this.executedCount;
    }

    public void countApiCaseRunResult(List<ExecuteResultCountDTO> executeResultCountDTOList) {
        for (ExecuteResultCountDTO execResult : executeResultCountDTOList) {
            if (StringUtils.isEmpty(execResult.getExecResult())) {
                this.notExecutedCount += execResult.getCount();
            } else if (StringUtils.equalsAnyIgnoreCase(execResult.getExecResult(), ApiTestDataStatus.UNDERWAY.getValue(),
                    ApiReportStatus.STOPPED.name())) {
                this.notExecutedCount += execResult.getCount();
            } else if (StringUtils.equalsIgnoreCase(execResult.getExecResult(), ApiReportStatus.SUCCESS.name())) {
                this.passCount += execResult.getCount();
            } else if (StringUtils.equalsAnyIgnoreCase(execResult.getExecResult(), ApiReportStatus.FAKE_ERROR.name())) {
                fakeErrorCount += execResult.getCount();
            } else {
                this.unPassCount += execResult.getCount();
            }
        }
        this.executedCount = this.unPassCount + this.fakeErrorCount + this.passCount;
    }

    public void countScheduleExecute(List<ApiDataCountResult> allExecuteResult) {
        for (ApiDataCountResult countResult : allExecuteResult) {
            if (StringUtils.equalsIgnoreCase(countResult.getGroupField(), ApiReportStatus.SUCCESS.name())) {
                this.passCount += countResult.getCountNumber();
            } else if (StringUtils.equalsAnyIgnoreCase(countResult.getGroupField(), ApiReportStatus.ERROR.name())) {
                this.unPassCount += countResult.getCountNumber();
            } else if (StringUtils.equalsAnyIgnoreCase(countResult.getGroupField(), ApiReportStatus.FAKE_ERROR.name(), "errorReportResult")) {
                this.fakeErrorCount += countResult.getCountNumber();
            }
        }
    }

    /**
     * 统计覆盖率相关数据
     *
     * @param coverageMap 和覆盖率相关的protocol集合
     * @param isUnCovered 是否统计的是未覆盖的数据
     */
    public void countCovered(Map<String, List<ApiDefinition>> coverageMap, boolean isUnCovered) {
        if (MapUtils.isNotEmpty(coverageMap)) {
            for (Map.Entry<String, List<ApiDefinition>> entry : coverageMap.entrySet()) {
                if (CollectionUtils.isNotEmpty(entry.getValue())) {
                    switch (entry.getKey()) {
                        case RequestTypeConstants.DUBBO:
                            if (isUnCovered) {
                                this.rpcNotCovered += entry.getValue().size();
                            } else {
                                this.rpcCovered += entry.getValue().size();
                            }
                            break;
                        case RequestTypeConstants.HTTP:
                            if (isUnCovered) {
                                this.httpNotCovered += entry.getValue().size();
                            } else {
                                this.httpCovered += entry.getValue().size();
                            }
                            break;
                        case RequestTypeConstants.SQL:
                            if (isUnCovered) {
                                this.sqlNotCovered += entry.getValue().size();
                            } else {
                                this.sqlCovered += entry.getValue().size();
                            }
                            break;
                        case RequestTypeConstants.TCP:
                            if (isUnCovered) {
                                this.tcpNotCovered += entry.getValue().size();
                            } else {
                                this.tcpCovered += entry.getValue().size();
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        if (isUnCovered) {
            this.notCoveredCount = this.rpcNotCovered + this.httpNotCovered + this.tcpNotCovered + this.sqlNotCovered;
        } else {
            this.coveredCount = this.rpcCovered + this.httpCovered + this.tcpCovered + this.sqlCovered;
        }
    }
}
