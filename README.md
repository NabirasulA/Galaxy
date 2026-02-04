# 🌌 Galaxy – Portfolio Management System  

Galaxy is a full-stack portfolio management application designed to help users track their stock investments, analyze daily performance, and view real-time market insights through an intuitive and visually rich dashboard.  

---  

## 🚀 Features  

### ✅ Core Portfolio Management 
- Add, update, sell, and remove stocks 
- Automatic average buy price calculation 
- Real-time portfolio value updates  

### 📊 Portfolio Performance Analysis 
- Daily portfolio snapshot generation 
- Gain or loss calculation by comparing previous day performance 
- Clear profit/loss messaging for users  

### 📈 Market Insights 
- Top gainers and top losers 
- Most active stocks 
- Live market data integration  

### 🎨 Interactive Dashboard 
- Galaxy-themed UI with charts and visualizations 
- Portfolio distribution using pie chart 
- Performance trend visualization 
- Clean and responsive design  

---  

## 🛠️ Tech Stack  

### Backend 
- Java 
- Spring Boot 
- Spring Data JPA 
- Hibernate 
- MySQL  

### Frontend 
- HTML 
- CSS 
- JavaScript  

### External APIs 
- Alpha Vantage (Market Data) 
- IPO Alerts API (Upcoming IPOs) 
- Finnub API (Searching stocks and Live Data)  

---  

## 🗂️ Project Structure 

---  

## 📅 Sprint Planning Overview  

### Sprint 1 – Core Portfolio Management ✅ 
- Portfolio CRUD operations 
- Backend REST APIs 
- Portfolio table & pie chart  

### Sprint 2 – Performance & Market Insights ✅ 
- Daily portfolio snapshot logic 
- Profit/loss calculation 
- Market gainers, losers & active stocks  

### Sprint 3 – Future Scope 🚧 
- SMS notifications for portfolio performance 
- User notification preferences 
- Smart alerts and scheduled notifications  

---  

## 🔮 Future Enhancements 
- SMS notifications via Twilio/AWS SNS 
- User authentication & authorization 
- Advanced analytics and trend predictions 
- Notification history and alert customization  

---  

## ▶️ How to Run the Project

### Prerequisites
Ensure the following are installed on your system:
- Java JDK 17 or above
- Maven
- MySQL Server
- Git
- A modern web browser (Chrome / Edge)

---

### Backend Setup (Spring Boot)

1. Clone the repository:
   ```bash
   git clone https://github.com/<your-username>/Galaxy.git
   cd Galaxy
   ```

2. Create the MySQL database:
   ```sql
   CREATE DATABASE portfoliomanagement;
   ```

3. Configure database credentials in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/portfoliomanagement
   spring.datasource.username=root
   spring.datasource.password=your_mysql_password
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true
   ```

4. Add external API keys in `application.properties`:
   ```properties
   alphavantage.api.key=YOUR_ALPHA_VANTAGE_KEY
   alphavantage.base.url=https://www.alphavantage.co
   finnhub.api.key=YOUR_FINNHUB_KEY
   ```

5. Run the Spring Boot application:
   - Open `PortfolioManagementApplication.java` directly from IntelliJ.

6. Backend will start at:
   ```
   http://localhost:8080/
   ```
