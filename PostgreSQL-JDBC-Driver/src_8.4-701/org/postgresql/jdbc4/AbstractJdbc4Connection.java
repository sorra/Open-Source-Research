/*-------------------------------------------------------------------------
*
* Copyright (c) 2004-2008, PostgreSQL Global Development Group
*
* IDENTIFICATION
*   $PostgreSQL: pgjdbc/org/postgresql/jdbc4/AbstractJdbc4Connection.java,v 1.7 2008/10/08 18:24:05 jurka Exp $
*
*-------------------------------------------------------------------------
*/
package org.postgresql.jdbc4;

import java.sql.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.postgresql.core.Oid;
import org.postgresql.core.TypeInfo;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLState;
import org.postgresql.util.PSQLException;
import org.postgresql.jdbc2.AbstractJdbc2Array;

//这个类包含了1.6新加的11个方法的实现:
//6个createXXX方法
//isValid
//4个与ClientInfo相关的方法
abstract class AbstractJdbc4Connection extends org.postgresql.jdbc3g.AbstractJdbc3gConnection
{
    Properties _clientInfo;

    public AbstractJdbc4Connection(String host, int port, String user, String database, Properties info, String url) throws SQLException {
        super(host, port, user, database, info, url);

        TypeInfo types = getTypeInfo();
        if (haveMinimumServerVersion("8.3")) {
            types.addCoreType("xml", Oid.XML, java.sql.Types.SQLXML, "java.sql.SQLXML", Oid.XML_ARRAY);
        }
    }

	//1.6新加的6个createXXX方法，只支持createSQLXML和createArrayOf
    public Clob createClob() throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "createClob()");
    }

    public Blob createBlob() throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "createBlob()");
    }

    public NClob createNClob() throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "createNClob()");
    }

    public SQLXML createSQLXML() throws SQLException
    {
        return new Jdbc4SQLXML(this);
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "createStruct(String, Object[])");
    }

    public Array createArrayOf(String typeName, Object[] elements) throws SQLException
    {
        int oid = getTypeInfo().getPGArrayType(typeName);
        if (oid == Oid.UNSPECIFIED)
            throw new PSQLException(GT.tr("Unable to find server array type for provided name {0}.", typeName), PSQLState.INVALID_NAME);

        StringBuffer sb = new StringBuffer();
        appendArray(sb, elements);

        // This will not work once we have a JDBC 5,
        // but it'll do for now.
        return new Jdbc4Array(this, oid, sb.toString());
    }

	//因为有可能是多维数组，所以这里使用了Object类型
    private static void appendArray(StringBuffer sb, Object elements)
    {
        sb.append('{');

        int nElements = java.lang.reflect.Array.getLength(elements);
        for (int i=0; i<nElements; i++) {
            if (i > 0) {
                sb.append(',');
            }

            Object o = java.lang.reflect.Array.get(elements, i);
            if (o == null) {
                sb.append("NULL");
            } else if (o.getClass().isArray()) {
                appendArray(sb, o);
            } else {
                String s = o.toString();
                AbstractJdbc2Array.escapeArrayElement(sb, s);
            }
        }
        sb.append('}');
    }

    public boolean isValid(int timeout) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "isValid(int)");
    }

    public void setClientInfo(String name, String value) throws SQLClientInfoException
    {
		//是指java.sql.ClientInfoStatus
        Map<String, ClientInfoStatus> failures = new HashMap<String, ClientInfoStatus>();
        failures.put(name, ClientInfoStatus.REASON_UNKNOWN_PROPERTY);
        throw new SQLClientInfoException(GT.tr("ClientInfo property not supported."), failures);
    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException
    {
        if (properties == null || properties.size() == 0)
            return;

        Map<String, ClientInfoStatus> failures = new HashMap<String, ClientInfoStatus>();

        Iterator<String> i = properties.stringPropertyNames().iterator();
        while (i.hasNext()) {
            failures.put(i.next(), ClientInfoStatus.REASON_UNKNOWN_PROPERTY);
        }
        throw new SQLClientInfoException(GT.tr("ClientInfo property not supported."), failures);
    }

    public String getClientInfo(String name) throws SQLException
    {
        return null;
    }

    public Properties getClientInfo() throws SQLException
    {
        if (_clientInfo == null) {
            _clientInfo = new Properties();
        }
        return _clientInfo;
    }

	//没用到
    public <T> T createQueryObject(Class<T> ifc) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "createQueryObject(Class<T>)");
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "isWrapperFor(Class<?>)");
    }

    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "unwrap(Class<T>)");
    }


}
