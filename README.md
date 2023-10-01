# Backend-Java-Tomcat
KUIT 서버 파트 - 3주차 미션 수행을 위한 레포지토리입니다.

## 미션
- 미션은 [Backend-Java-Tomcat](https://github.com/KUIT-2/Backend-Java-Tomcat) 저장소를 Fork & Clone해 시작
- 자세한 내용은 디스코드 공지 참고

## 제출 방법
- [미션 제출 방법](https://iron-tumbleweed-cb2.notion.site/f3b4e86d10b94511b262e17c71fedcdf?pvs=4)을 참고하여 제출

## 질문사항
- 이 코드 실행시킬때 WebServer클래스 실행시키고 cmd창에 http://localhost:{port}/ (port에는 포트 번호를 적어야 할 것 같은데
8080이나 8000 적어도 연결 거부되길래 노션 페이지 그대로 {port}라고 적었습니다.} 이런식으로 적고
링크에 들어갔는데 이렇게 하는게 맞는지
- 리다이렉트 명령 수행할때 base code에는 response302Header(dos,HOME_URL); 이런식으로 해놓았는데
저는 저렇게 하니깐 페이지 오류가(연결이 거부되었다는 메시지) 뜨고 response302Header(dos,"../") 이런식으로 이전
루트로 돌아가라는 argument를 기입해야 과제 요구사항처럼 동작하는데 왜 이렇게 되는지 모르겠습니다.
- status code 302 redirect에 대해 알아보라고 했는데 검색을 해도 잘 모르겠어서 튜터님께서 아시는바가 있다면 말씀해주시면
감사하겠습니다.
- 과제 2에는 분명 GET방식으로 회원가입한다고 되어있는데 과제2 구현들어가기 전에 회원가입 정보를 입력하고 회원가입 버튼을 누르니
창 위에 url에는 /user/signup 밖에 안뜨고 &userId=~~ 이런 정보는 뜨지 않아서 request 정보를 찍어보니 이미 POST방식으로 설정되어 있었습니다.
그래서 과제2는 구현못했고 바로 과제3으로 건너뛰었는데 괜찮을까요?
- 마지막으로 과제7에 CSS 어떻게 적용하는지 힌트를 봐도 잘 모르겠습니다.