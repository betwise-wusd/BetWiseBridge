package com.betwise.betwisebridge.utils

import android.content.Context
import android.content.SharedPreferences
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class SPUtils {
    companion object{
        /**
         * 保存在手机里面的文件名
         */
        val FILE_NAME = "wayki_times_share_data"

        /**
         * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
         *
         * @param context
         * @param key
         * @param content
         */
        fun put(context: Context, key: String, content: Any?) {
            var content = content

            if (content == null) {
                content = ""
            }

            val sp = context.getSharedPreferences(
                FILE_NAME,
                Context.MODE_PRIVATE
            )
            val editor = sp.edit()

            if (content is String) {
                editor.putString(key, content as String?)
            } else if (content is Int) {
                editor.putInt(key, (content as Int?)!!)
            } else if (content is Boolean) {
                editor.putBoolean(key, (content as Boolean?)!!)
            } else if (content is Float) {
                editor.putFloat(key, (content as Float?)!!)
            } else if (content is Long) {
                editor.putLong(key, (content as Long?)!!)
            } else {
                editor.putString(key, content.toString())
            }

            SharedPreferencesCompat.apply(editor)
        }

        /**
         * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
         *
         * @param context
         * @param key
         * @param defaultObject
         * @return
         */
        fun get(context: Context, key: String, defaultObject: Any): Any? {
            val sp = context.getSharedPreferences(
                FILE_NAME,
                Context.MODE_PRIVATE
            )

            return when (defaultObject) {
                is String -> sp.getString(key, defaultObject)
                is Int -> sp.getInt(key, defaultObject)
                is Boolean -> sp.getBoolean(key, defaultObject)
                is Float -> sp.getFloat(key, defaultObject)
                is Long -> sp.getLong(key, defaultObject)
                else -> null
            }

        }

        /**
         * 移除某个 key 值已经对应的值
         *
         * @param context
         * @param key
         */
        fun remove(context: Context, key: String) {
            val sp = context.getSharedPreferences(
                FILE_NAME,
                Context.MODE_PRIVATE
            )
            val editor = sp.edit()
            editor.remove(key)
            SharedPreferencesCompat.apply(editor)
        }

        /**
         * 清除所有数据
         *
         * @param context
         */
        fun clear(context: Context) {
            val sp = context.getSharedPreferences(
                FILE_NAME,
                Context.MODE_PRIVATE
            )
            val editor = sp.edit()
            editor.clear()
            SharedPreferencesCompat.apply(editor)
        }

        /**
         * 查询某个 key 是否已经存在
         *
         * @param context
         * @param key
         * @return
         */
        fun contains(context: Context, key: String): Boolean {
            val sp = context.getSharedPreferences(
                FILE_NAME,
                Context.MODE_PRIVATE
            )
            return sp.contains(key)
        }

        /**
         * 返回所有的键值对
         *
         * @param context
         * @return
         */
        fun getAll(context: Context): Map<String, *> {
            val sp = context.getSharedPreferences(
                FILE_NAME,
                Context.MODE_PRIVATE
            )
            return sp.all
        }
    }
}

/**
 * 创建一个解决 SharedPreferencesCompat.apply 方法的一个兼容类
 *
 * @author zhy
 */
private object SharedPreferencesCompat {
    private val sApplyMethod = findApplyMethod()

    /**
     * 反射查找 apply 的方法
     *
     * @return
     */
    private fun findApplyMethod(): Method? {
        try {
            val clz = SharedPreferences.Editor::class.java
            return clz.getMethod("apply")
        } catch (e: NoSuchMethodException) {
        }

        return null
    }

    /**
     * 如果找到则使用 apply 执行，否则使用 commit
     *
     * @param editor
     */
    fun apply(editor: SharedPreferences.Editor) {
        try {
            if (sApplyMethod != null) {
                sApplyMethod.invoke(editor)
                return
            }
        } catch (e: IllegalArgumentException) {
        } catch (e: IllegalAccessException) {
        } catch (e: InvocationTargetException) {
        }

        editor.commit()
    }
}

