package zysy.iflytek.zhumang.common.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.Duration;

/**
 * @author yangxin
 * @date 2022/12/7 18:41
 * @description: 发送邮箱业务
 */
@Component
public class MailMsg {
    private static final Logger logger = LoggerFactory.getLogger(MailMsg.class);

    @Resource
    private JavaMailSenderImpl mailSender;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    public boolean mail(String email) throws MessagingException {
        try {
            // 生成随机验证码
            String code = CodeGeneratorUtil.generateCode(6);
            logger.info("生成验证码: {} 发送到邮箱: {}", code, email);

            // 创建邮件
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            
            // 设置邮件内容
            helper.setText("<p style='color: blue'>你的验证码为：" + code + "(有效期为5分钟)</p>", true);
            helper.setSubject("助盲APP验证码");
            helper.setTo(email);
            helper.setFrom("2280929569@qq.com");

            // 将验证码存入Redis，5分钟过期
            redisTemplate.opsForValue().set(email, code, Duration.ofMinutes(5));
            logger.info("验证码已存入Redis，key: {}", email);

            // 发送邮件
            mailSender.send(mimeMessage);
            logger.info("验证码邮件发送成功");
            
            return true;
        } catch (Exception e) {
            logger.error("发送验证码失败", e);
            throw e;
        }
    }
}
