package com.duyp.architecture.clean.redux.app.common

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.*
import com.duyp.architecture.clean.redux.BuildConfig
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

inline fun <T : Fragment> T.withArguments(bundleConsumer: Bundle.() -> Unit): T {
    val bundle = Bundle()
    bundleConsumer(bundle)
    arguments = bundle
    return this
}

fun <T> Fragment.observe(data: LiveData<T>, consumer: (T) -> Unit) {
    data.observe(this, Observer {
        consumer(it)
    })
}

fun <T> FragmentActivity.observe(data: LiveData<T>, consumer: (T) -> Unit) {
    data.observe(this, Observer {
        consumer(it)
    })
}

inline fun FragmentManager.inTransaction(transaction: FragmentTransaction.() -> Unit) {
    val ft = this.beginTransaction()
    transaction(ft)
    ft.commit()
}

inline fun <reified T> FragmentManager.findTypedFragmentByTag(tag: String): T? {
    val f = this.findFragmentByTag(tag)
    return if (f is T) f else null
}

fun FragmentManager.showFragment(
    tag: String,
    @IdRes containerId: Int,
    @AnimRes transitionIn: Int = 0,
    @AnimRes transitionOut: Int = 0,
    hideAllOthers: Boolean = true,
    createIfNotExisted: () -> Fragment
) {
    inTransaction {
        setCustomAnimations(transitionIn, transitionOut)
        // find fragment by tag, create if not found
        val fragment = findFragmentByTag(tag) ?: createIfNotExisted()
        if (fragment.isAdded) {
            // show if already added
            show(fragment)
        } else {
            // add if not added yet
            add(containerId, fragment, tag)
        }
        if (hideAllOthers) {
            fragments.forEach {
                if (it != fragment) {
                    hide(it)
                }
            }
        }
    }
}

inline fun <reified VM : ViewModel> Fragment.getViewModel(factory: ViewModelProvider.Factory): VM {
    return ViewModelProviders.of(this, factory).get(VM::class.java)
}

inline fun <reified VM : ViewModel> FragmentActivity.getViewModel(factory: ViewModelProvider.Factory): VM {
    return ViewModelProviders.of(this, factory).get(VM::class.java)
}

fun ViewGroup.inflate(@LayoutRes resource: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(this.context).inflate(resource, this, attachToRoot)
}

fun View.setVisible(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.GONE
}

fun TextView.setTextOrHideIfEmpty(value: String?) {
    setVisible(!value.isNullOrEmpty())
    text = value
}

fun EditText.onTextChanged(): Observable<String> {
    return Observable.create<String> { emitter ->
        val textWatcher = object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                s?.toString()?.let {
                    emitter.onNext(it)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        }
        this.addTextChangedListener(textWatcher)
        emitter.setCancellable {
            this.removeTextChangedListener(textWatcher)
        }
    }
}


fun Throwable.printIfDebug() {
    if (BuildConfig.DEBUG) {
        this.printStackTrace()
    }
}

fun Disposable.addTo(disposables: CompositeDisposable) {
    disposables.add(this)
}

inline fun <T, reified R : T> Iterable<T>.findTyped(): R? {
    val item = firstOrNull { it is R }
    return if (item is R) item else null
}
