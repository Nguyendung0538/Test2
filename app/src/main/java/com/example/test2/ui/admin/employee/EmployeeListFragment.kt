/**
 * Fragment hiển thị danh sách toàn bộ nhân viên dành cho Quản trị viên (Admin).
 * Hỗ trợ tìm kiếm realtime, hiển thị trạng thái danh sách rỗng (Empty State)
 * và chuyển sang màn hình Chi tiết hoặc Thêm mới nhân viên.
 */
package com.example.test2.ui.admin.employee

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.test2.PayrollApplication
import com.example.test2.R
import com.example.test2.databinding.FragmentEmployeeListBinding
import kotlinx.coroutines.launch

class EmployeeListFragment : Fragment() {

    private var _binding: FragmentEmployeeListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EmployeeListViewModel by viewModels {
        val app = requireActivity().application as PayrollApplication
        EmployeeListViewModel.Factory(app.container.employeeRepository)
    }

    private lateinit var adapter: EmployeeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployeeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()
        setupFab()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = EmployeeAdapter { employee ->
            // Bấm vào từng dòng nhân viên -> Mở màn hình Chi tiết Nhân viên
            openEmployeeDetail(employee.id)
        }
        binding.rvEmployees.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.setSearchQuery(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText.orEmpty())
                return true
            }
        })
    }

    private fun setupFab() {
        binding.fabAddEmployee.setOnClickListener {
            // Mở màn hình Form ở chế độ Thêm mới
            openEmployeeForm(employeeId = null)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.employees.collect { list ->
                    adapter.submitList(list)
                    binding.layoutEmptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    binding.rvEmployees.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }

    private fun openEmployeeDetail(employeeId: Int) {
        val fragment = EmployeeDetailFragment.newInstance(employeeId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun openEmployeeForm(employeeId: Int?) {
        val fragment = EmployeeFormFragment.newInstance(employeeId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = EmployeeListFragment()
    }
}