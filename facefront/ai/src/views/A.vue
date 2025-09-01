<!--&lt;!&ndash;vue3组件，但我们需要用vue2来写&ndash;&gt;-->
<!--&lt;!&ndash;<script setup>&ndash;&gt;-->
<!--&lt;!&ndash;import { ref } from 'vue'&ndash;&gt;-->

<!--&lt;!&ndash;const message = ref("我的第一个vue-cli页面")&ndash;&gt;-->

<!--&lt;!&ndash;function handleClick() {}&ndash;&gt;-->
<!--&lt;!&ndash;</script>&ndash;&gt;-->

<!--<template>-->
<!--&lt;!&ndash;  只能有一个div&ndash;&gt;-->
<!--  <div>-->
<!--&lt;!&ndash;    {{插值}}}&ndash;&gt;-->
<!--    <h1>{{message}}</h1>-->

<!--    <form v-model = "registerForm">-->
<!--      用户名： <input type="text" v-model="registerForm.username">-->
<!--      密码： <input type="password" v-model="registerForm.password"></input>-->
<!--      <button v-on:click="loginForm" >提交</button>>-->
<!--    </form>-->
<!--  </div>-->
<!--</template>-->

<!--<script>-->
<!--import {registerRef} from "vue/src/core/vdom/modules/template-ref";-->

<!--export default {-->
<!--    name: "A",-->
<!--  methods: {registerRef},-->
<!--    data(){-->
<!--      return{-->
<!--        message:"我的第一个vue-cli页面",-->
<!--        registerForm:{-->
<!--          username: 'admin',-->
<!--          password: '123456',-->
<!--        }-->
<!--      }-->
<!--    }-->

<!--}-->
<!--</script>-->

<!--<style scoped lang="less">-->



<!--</style>-->