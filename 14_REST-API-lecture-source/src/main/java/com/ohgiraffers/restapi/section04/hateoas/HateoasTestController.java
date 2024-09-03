package com.ohgiraffers.restapi.section04.hateoas;

import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/hateoas")
public class HateoasTestController {

    /*
    * Hateoas 란?
    * 하나의 api를 동작시켰을 때, 그 전 상황이나 그 후 상황에 대한 링크를
    * 제공함으로써 클라이언트가 리소스(자원)에 대한 이해를 더욱 충분히
    * 할 수 있도록 링크를 제공하는 것
    *
    * (Hateoas까지 구현했을때, Restfull의 성숙도 3단계 API(최종단계) 라고 한다.)
    * */

    private List<UserDTO> users;

    public HateoasTestController() {
        users = new ArrayList<>();

        users.add(new UserDTO(1, "user01", "pass01", "너구리", LocalDate.now()));
        users.add(new UserDTO(2, "user02", "pass02", "코알라", LocalDate.now()));
        users.add(new UserDTO(3, "user03", "pass03", "곰", LocalDate.now()));
    }

    @GetMapping("/users/{userNo}")
    public ResponseEntity<ResponseMessage> findUserByNo(@PathVariable int userNo) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));


        UserDTO foundUser = users.stream().filter(user -> user.getNo() == userNo).toList().get(0);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("user", foundUser);

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new ResponseMessage(200, "조회 성공", responseMap));
    }

    // Hateoas 적용한 전체 조회
    @GetMapping("/users")
    public ResponseEntity<ResponseMessage> findAllUsers() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

        // EntityModel : hateoas 제공하는 클래스 rest api를 만들때 해당 자원과 관련된 링크를 포함 할 수 있게 해준다.
        List<EntityModel<UserDTO>> userWithRel
                = users.stream().map(user -> EntityModel.of(
                        user,
                        linkTo(methodOn(HateoasTestController.class).findUserByNo(user.getNo())).withSelfRel(),
                        linkTo(methodOn(HateoasTestController.class).findAllUsers()).withRel("users")
        )).toList();

        Map<String, Object> responseMap = new HashMap<>();

        responseMap.put("user", userWithRel);

        return ResponseEntity.ok()
                .headers(headers)
                .body(new ResponseMessage(200, "전체조회성공", responseMap));
    }
}
