package com.example.bogoargo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import java.util.UUID // 초대 코드 생성을 위해 UUID 임포트

// 초대 코드 생성 유틸리티 (이전 답변에서 설명된 무작위 문자열 생성 로직을 간소화)
// 실제 앱에서는 이 로직은 서버에서 안전하게 수행되어야 합니다.

import kotlin.random.Random


object InvitationCodeGenerator {
    private val CHAR_POOL: CharArray = (('A'..'Z').toList() + ('2'..'9').toList()) // ✨ 두 CharRange를 List로 변환 후 합칩니다.
        .filter { // ✨ 이제 List에 대해 filter를 사용할 수 있습니다.
            it != 'O' && it != 'I' && it != '0' && it != '1'
        }
        .toCharArray() // 다시 CharArray로 변환합니다.

    fun generateSimpleCode(length: Int = 8): String { // 8자리 코드로 변경
        return (1..length)
            .map { Random.nextInt(0, CHAR_POOL.size) }
            .map(CHAR_POOL::get)
            .joinToString("")
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClassScreen(navController: NavController) {
    var schoolName by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("") }
    var maxStudents by remember { mutableStateOf("") } // Int로 바로 받지 않고 String으로 받아서 파싱
    var description by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("새로운 반 생성", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp) // 각 입력 필드 사이 간격
        ) {
            // 학교명 입력
            OutlinedTextField(
                value = schoolName,
                onValueChange = { schoolName = it },
                label = { Text("학교명") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 반 이름 입력
            OutlinedTextField(
                value = className,
                onValueChange = { className = it },
                label = { Text("반 이름 (예: 1학년 1반)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 최대 인원 입력 (숫자만)
            OutlinedTextField(
                value = maxStudents,
                onValueChange = { newValue ->
                    // 숫자가 아닌 문자 제거
                    maxStudents = newValue.filter { it.isDigit() }
                },
                label = { Text("최대 인원") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            // 설명 입력 (여러 줄)
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("설명 (선택 사항)") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp), // 최소 높이 지정
                maxLines = 5 // 최대 5줄
            )

            var region by remember { mutableStateOf("") } // 선택된 지역
            var expanded by remember { mutableStateOf(false) } // 드롭다운 메뉴 확장 상태
            val focusRequester = remember { FocusRequester() } // TextField 포커스 제어

            // 예시 지역 목록 (실제 앱에서는 API에서 가져오거나 미리 정의된 전체 목록)
            val regions = remember {
                listOf(
                    "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종", "경기",
                    "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"
                )
            }

            // ... (Scaffold 및 Column 시작 부분) ...

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ... (학교명, 반 이름, 최대 인원, 설명 필드) ...

                // 지역 선택 드롭다운
                ExposedDropdownMenuBox( // ExposedDropdownMenuBox 사용
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = region,
                        onValueChange = { }, // 사용자가 직접 입력 못하게 비활성화
                        readOnly = true, // 읽기 전용으로 설정
                        label = { Text("지역 선택") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor() // ExposedDropdownMenuBox의 앵커 역할
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        regions.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    region = selectionOption
                                    expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.DropdownMenuItemContentPadding
                            )
                        }
                    }
                }

                // ... (생성 완료 버튼 및 나머지 코드) ...
            }
            // ... (Scaffold 및 Column 끝 부분) ...

            Spacer(modifier = Modifier.height(8.dp)) // 버튼 위 여백

            // 생성 완료 버튼
            Button(
                onClick = {
                    // 유효성 검사 (필수 필드 확인)
                    if (schoolName.isBlank() || className.isBlank() || maxStudents.isBlank() || region.isBlank()) {
                        // TODO: 사용자에게 오류 메시지 표시 (예: Toast, Snackbar)
                        println("모든 필수 정보를 입력해주세요.")
                        return@Button
                    }

                    // 최대 인원 파싱
                    val maxStudentsInt = maxStudents.toIntOrNull()
                    if (maxStudentsInt == null || maxStudentsInt <= 0) {
                        // TODO: 유효하지 않은 인원수 오류 메시지 표시
                        println("유효한 최대 인원수를 입력해주세요.")
                        return@Button
                    }

                    // 더미 초대 코드 생성 (실제는 서버에서 받아와야 함)
                    val invitationCode = InvitationCodeGenerator.generateSimpleCode()
                    println("생성된 초대 코드: $invitationCode")

                    // 여기서 실제 서버 API 호출하여 반 정보 저장 및 초대 코드 발급 로직 수행
                    // 예: yourClassRepository.createClass(schoolName, className, maxStudentsInt, description, region)
                    //     .onSuccess { response ->
                    //         val actualInvitationCode = response.invitationCode
                    //         navController.navigate("classInfoPage/$classId?code=$actualInvitationCode")
                    //     }
                    //     .onFailure { error -> /* 오류 처리 */ }

                    // 생성이 완료되면 현재 반 정보 페이지로 이동 (초대 코드 전달)
                    // "currentClassInfo"는 다음 페이지의 라우트 이름이 됩니다.
                    // 쿼리 파라미터로 초대 코드와 반 정보를 함께 전달합니다.
                    navController.navigate(
                        "currentClassInfo/" +
                                "$schoolName/" +
                                "$className/" +
                                "$maxStudents/" +
                                "$description/" +
                                "$region/" +
                                "$invitationCode"
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("반 생성 완료", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FocusRequester() {
    TODO("Not yet implemented")
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun PreviewCreateClassScreen() {
    val navController = rememberNavController()
    CreateClassScreen(navController = navController)
}