package com.review.agent.infrastructure.security;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.security.SecureRandom;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CredentialStringTypeHandler extends BaseTypeHandler<String> {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public void setNonNullParameter(
            PreparedStatement ps, int index, String parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(index, CredentialEncryptionManager.encryptValue(parameter, secureRandom));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return CredentialEncryptionManager.decryptValue(rs.getString(columnName));
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return CredentialEncryptionManager.decryptValue(rs.getString(columnIndex));
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return CredentialEncryptionManager.decryptValue(cs.getString(columnIndex));
    }
}
