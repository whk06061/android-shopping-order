# android-shopping-cart

- [x] 데이터 로딩 전 스켈레톤 UI를 띄운다
- [x] 상품 목록과 장바구니 목록 서버 연동 작업 추가
- [x] 사용자 인증 정보를 저장한다
- [x] 메인 액티비티가 로딩되기 전, 서버를 선택하여 교체할 수 있다

# android-shopping-order

## 주문 페이지

- [x] 화면에 주문할 상품 목록이 나타난다.

```gherkin
Given cartId 를 이용해 상품을 받아올 수 있는 상태이다.
When cartId 를 통해 상품을 요청한다.
Then 주문할 상품 목록이 노출된다.
```

- [x] 주문 금액에 따른 할인 정보를 계산한다.
    - [x] 주문 금액이 할인 정책에 해당하지 않는다면 할인 정보를 띄우지 않는다.
    - [x] 주문 금액이 할인 정책에 해당한다면 할인 정보를 띄운다.
    ```gherkin
    Given 주문할 상품 불러오기가 완료되어 주문 금액을 계산할 수 있는 상태이다.
    When 주문할 상품의 가격을 합산하고 그에 따른 할인 금액을 계산한다.
    Then 할인 가능 여부를 알 수 있다.
    ```

- [ ] 주문을 할 수 있다.

```gherkin
Given cartId 와 최종 주문 금액을 알고있는 상태이다.
When 주문 요청을 보낸다.
Then 주문 완료 화면을 노출한다.
```