//
// Copyright (c) ZeroC, Inc. All rights reserved.
//
//
// Ice version 3.7.2
//
// <auto-generated>
//
// Generated from file `bank.ice'
//
// Warning: do not edit this file.
//
// </auto-generated>
//

package Banking;

public class Account implements java.lang.Cloneable,
                                java.io.Serializable
{
    public AccountType type;

    public String password;

    public Account()
    {
        this.type = AccountType.STANDARD;
        this.password = "";
    }

    public Account(AccountType type, String password)
    {
        this.type = type;
        this.password = password;
    }

    public boolean equals(java.lang.Object rhs)
    {
        if(this == rhs)
        {
            return true;
        }
        Account r = null;
        if(rhs instanceof Account)
        {
            r = (Account)rhs;
        }

        if(r != null)
        {
            if(this.type != r.type)
            {
                if(this.type == null || r.type == null || !this.type.equals(r.type))
                {
                    return false;
                }
            }
            if(this.password != r.password)
            {
                if(this.password == null || r.password == null || !this.password.equals(r.password))
                {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    public int hashCode()
    {
        int h_ = 5381;
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, "::Banking::Account");
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, type);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, password);
        return h_;
    }

    public Account clone()
    {
        Account c = null;
        try
        {
            c = (Account)super.clone();
        }
        catch(CloneNotSupportedException ex)
        {
            assert false; // impossible
        }
        return c;
    }

    public void ice_writeMembers(com.zeroc.Ice.OutputStream ostr)
    {
        AccountType.ice_write(ostr, this.type);
        ostr.writeString(this.password);
    }

    public void ice_readMembers(com.zeroc.Ice.InputStream istr)
    {
        this.type = AccountType.ice_read(istr);
        this.password = istr.readString();
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, Account v)
    {
        if(v == null)
        {
            _nullMarshalValue.ice_writeMembers(ostr);
        }
        else
        {
            v.ice_writeMembers(ostr);
        }
    }

    static public Account ice_read(com.zeroc.Ice.InputStream istr)
    {
        Account v = new Account();
        v.ice_readMembers(istr);
        return v;
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, int tag, java.util.Optional<Account> v)
    {
        if(v != null && v.isPresent())
        {
            ice_write(ostr, tag, v.get());
        }
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, int tag, Account v)
    {
        if(ostr.writeOptional(tag, com.zeroc.Ice.OptionalFormat.FSize))
        {
            int pos = ostr.startSize();
            ice_write(ostr, v);
            ostr.endSize(pos);
        }
    }

    static public java.util.Optional<Account> ice_read(com.zeroc.Ice.InputStream istr, int tag)
    {
        if(istr.readOptional(tag, com.zeroc.Ice.OptionalFormat.FSize))
        {
            istr.skip(4);
            return java.util.Optional.of(Account.ice_read(istr));
        }
        else
        {
            return java.util.Optional.empty();
        }
    }

    private static final Account _nullMarshalValue = new Account();

    /** @hidden */
    public static final long serialVersionUID = -1559979765L;
}