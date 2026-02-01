package com.argo.backend.global.enums;

import lombok.Getter;

@Getter
public enum ResponseMessage {
    // Auth
    SIGNUP_SUCCESS("회원 가입에 성공하였습니다."),
    LOGIN_SUCCESS("로그인에 성공했습니다."),
    WITHDRAW_SUCCESS("회원 탈퇴되었습니다."),
    SUCCESS_LOGOUT("로그아웃 되었습니다."),

    USER_NOT_FOUND_EXCEPTION("사용자를 찾을 수 없습니다."),
    PASSWORD_NOT_MATCH_EXCEPTION("비밀번호가 일치하지 않습니다."),
    SIGNUP_USERNAME_DUPLICATE_EXCEPTION("이미 존재하는 아이디입니다."),
    EXPIRED_TOKEN("만료된 토큰입니다."),
    INVALID_TOKEN("유효하지 않은 토큰입니다."),
    LOGGED_OUT_TOKEN("로그 아웃된 토큰입니다."),
    INVALID_CLAIM_TYPE("토큰에 클레임이 없거나 형식이 올바르지 않습니다."),
    ALREADY_WITHDRAW_USER("이미 탈퇴한 회원입니다."),

    // Parameter
    WRONG_REQUEST_PARAMETER("요청 파라미터가 잘못되었습니다."),

    // mission
    SUCCESS_CREATE_MISSION("미션 생성에 성공하였습니다."),
    SUCCESS_SUBMIT_MISSION("미션 제출에 성공하였습니다."),
    POSSIBLE_MISSION_SPOT("미션 수행이 가능한 장소입니다."),

    NOT_FOUND_MISSION_SESSION("해당 미션세션이 존재하지 않습니다."),
    INVALID_MISSION_SESSION("유효하지 않은 세션입니다."),
    TEAM_NOT_FOUND("해당 팀이 존재하지 않습니다."),
    SPOT_NOT_FOUND("해당 스팟이 존재하지 않습니다."),
    PROBLEM_NOT_FOUND("문제가 존재하지 않습니다."),
    CARD_NOT_EXIST("카드가 존재하지 않습니다."),
    ALREADY_COMPLETED_MISSION("해당 스팟에서 이미 미션 진행을 하였습니다."),

    // spotMission
    ALREADY_PROGRESSED_MISSION("이미 미션을 진행한 곳입니다."),

    // problem
    SUCCESS_GENERATE_PROBLEM("문제가 성공적으로 생성되었습니다."),
    SUCCESS_REGISTER_PROBLEM("문제가 성공적으로 등록되었습니다."),

    PROBLEM_TYPE_NOT_EXIST("문제 타입이 존재하지 않습니다."),

    // api
    PYTHON_SERVER_NO_RESPONSE("Python 서버에서 응답이 없습니다."),
    PROBLEM_GENERATION_FAILED("문제가 생성되지 않았습니다."),
    PROBLEM_COUNT_MISMATCH("요청한 문제 개수와 맞지 않습니다."),

    // gps
    SUCCESS_USER_COORDINATES_POST("유저 위치 정보 저장을 성공하였습니다."),
    SUCCESS_USERS_COORDINATES_RESPONSE("유저 위치 정보 리스트 반환에 성공하였습니다."),

    // cardgame
    CARD_INFO_SUCCESS("카드 정보 조회 성공"),
    TEAM_CARD_COLLECTION_SUCCESS("팀 카드 컬렉션 조회 성공"),
    BATTLE_OPPONENTS_SUCCESS("대전 가능한 팀 목록 조회 성공"),
    BATTLE_HISTORY_SUCCESS("대전 기록 조회 성공"),
    BATTLE_RESULT_VIEW_SUCCESS("대전 결과 확인 성공"),
    BATTLE_REQUEST_SENT("대전 신청이 성공적으로 전송되었습니다."),
    BATTLE_ACCEPTED("대전 수락이 완료되었습니다."),
    BATTLE_REJECTED("대전을 거절하였습니다."),
    BATTLE_CANCELLED("대전 신청이 취소되었습니다."),
    BATTLE_RESULT_VIEWED("대전 결과 확인이 처리되었습니다."),
    TEAM_STATS_SUCCESS("팀 통계 조회 성공"),

    // cardgame exceptions
    BATTLE_NOT_FOUND("해당 대전을 찾을 수 없습니다."),
    ALREADY_PROCESSED_BATTLE("이미 처리된 대전입니다."),
    TEAM_CARD_NOT_FOUND("해당 팀 카드를 찾을 수 없습니다."),
    CARD_NOT_OWNED_BY_TEAM("해당 카드는 이 팀의 소유가 아닙니다."),
    LOST_CARD_CANNOT_BE_USED("잃어버린 카드는 사용할 수 없습니다."),
    LOCKED_CARD_CANNOT_BE_USED("이미 사용 중인 카드입니다."),
    CANNOT_CANCEL_PROCESSED_BATTLE("이미 처리된 대전은 취소할 수 없습니다."),
    ONLY_COMPLETED_BATTLE_CAN_BE_VIEWED("완료된 대전만 결과를 확인할 수 있습니다."),

    // Global Exception
    DATA_INTEGRITY_VIOLATION("이미 처리된 요청이거나 중복된 데이터입니다."),
    ;

    private final String message;

    ResponseMessage(String message) {
        this.message = message;
    }
}
