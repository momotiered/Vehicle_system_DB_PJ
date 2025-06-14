<template>
  <div class="register-container">
    <h2>注册</h2>
    <form @submit.prevent="handleRegister">
      <div class="form-group">
        <label for="username">用户名</label>
        <input type="text" id="username" v-model="form.username" required>
      </div>
      <div class="form-group">
        <label for="password">密码</label>
        <input type="password" id="password" v-model="form.password" required>
      </div>
      <div class="form-group">
        <label for="fullName">全名</label>
        <input type="text" id="fullName" v-model="form.fullName">
      </div>
      <div class="form-group">
        <label for="contactPhone">联系电话</label>
        <input type="tel" id="contactPhone" v-model="form.contactPhone">
      </div>
      <div class="form-group">
        <label for="contactEmail">电子邮箱</label>
        <input type="email" id="contactEmail" v-model="form.contactEmail">
      </div>
      <div class="form-group">
        <label for="address">地址</label>
        <input type="text" id="address" v-model="form.address">
      </div>
      <button type="submit">注册</button>
      <p v-if="successMessage" class="success-message">{{ successMessage }}</p>
      <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
    </form>
    <p>已有账户? <router-link to="/login">立即登录</router-link></p>
  </div>
</template>

<script>
export default {
  data() {
    return {
      form: {
        username: '',
        password: '',
        fullName: '',
        contactPhone: '',
        contactEmail: '',
        address: ''
      },
      successMessage: '',
      errorMessage: ''
    };
  },
  methods: {
    async handleRegister() {
      this.successMessage = '';
      this.errorMessage = '';
      try {
        const response = await fetch('/api/users/register', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(this.form)
        });

        if (response.ok) {
          const newUser = await response.json();
          console.log('注册成功:', newUser);
          this.successMessage = '注册成功！现在您可以登录了。';
          // 可以在这里延迟几秒后跳转到登录页
          setTimeout(() => {
            // this.$router.push('/login');
          }, 2000);
        } else {
          const errorText = await response.text();
          this.errorMessage = errorText || '注册失败，请检查您输入的信息。';
        }
      } catch (error) {
        console.error('注册请求失败:', error);
        this.errorMessage = '无法连接到服务器，请稍后再试。';
      }
    }
  }
};
</script>

<style scoped>
.register-container {
  max-width: 400px;
  margin: 50px auto;
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 5px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}
h2 {
  text-align: center;
  margin-bottom: 20px;
}
.form-group {
  margin-bottom: 15px;
}
.form-group label {
  display: block;
  margin-bottom: 5px;
}
.form-group input {
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
}
button {
  width: 100%;
  padding: 10px;
  background-color: #28a745;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}
button:hover {
  background-color: #218838;
}
.error-message {
  color: red;
  margin-top: 10px;
}
.success-message {
  color: green;
  margin-top: 10px;
}
p {
    margin-top: 15px;
    text-align: center;
}
</style> 